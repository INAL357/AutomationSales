package com.app.AutomationSales.renewal.service;

import com.app.AutomationSales.config.RenewalProperties;
import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.notification.model.Notification;
import com.app.AutomationSales.notification.model.NotificationStatus;
import com.app.AutomationSales.notification.repository.NotificationRepository;
import com.app.AutomationSales.renewal.dto.RenewalAutomationResultDto;
import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.renewal.model.RenewalStatus;
import com.app.AutomationSales.renewal.repository.RenewalRepository;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salestask.model.SalesTask;
import com.app.AutomationSales.salestask.model.TaskPriority;
import com.app.AutomationSales.salestask.model.TaskStatus;
import com.app.AutomationSales.salestask.repository.SalesTaskRepository;
import com.app.AutomationSales.softwarelicense.model.LicenseStatus;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.softwarelicense.repository.SoftwareLicenseRepository;
import com.app.AutomationSales.softwarelicense.service.LicenseExpiryCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RenewalAutomationServiceImpl implements RenewalAutomationService {

    private static final Logger log = LoggerFactory.getLogger(RenewalAutomationServiceImpl.class);

    private static final List<RenewalStatus> OPEN_RENEWAL = List.of(RenewalStatus.PENDING, RenewalStatus.IN_PROGRESS);
    private static final List<RenewalStatus> CLOSED_RENEWAL = List.of(RenewalStatus.RENEWED, RenewalStatus.LOST);

    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final RenewalRepository renewalRepository;
    private final SalesTaskRepository salesTaskRepository;
    private final NotificationRepository notificationRepository;
    private final LicenseExpiryCalculator expiryCalculator;
    private final RenewalProperties renewalProperties;

    public RenewalAutomationServiceImpl(
            SoftwareLicenseRepository softwareLicenseRepository,
            RenewalRepository renewalRepository,
            SalesTaskRepository salesTaskRepository,
            NotificationRepository notificationRepository,
            LicenseExpiryCalculator expiryCalculator,
            RenewalProperties renewalProperties) {
        this.softwareLicenseRepository = softwareLicenseRepository;
        this.renewalRepository = renewalRepository;
        this.salesTaskRepository = salesTaskRepository;
        this.notificationRepository = notificationRepository;
        this.expiryCalculator = expiryCalculator;
        this.renewalProperties = renewalProperties;
    }

    @Override
    @Transactional   // keeps the DB session open so lazy fields (customer, product...) can be read
    public RenewalAutomationResultDto runDailyCheck() {

        int checked = 0, statusesUpdated = 0, renewalsCreated = 0,
                tasksCreated = 0, notificationsCreated = 0, skipped = 0;

        // Reminder stages sorted smallest first: [7, 14, 30, 60, 90]
        List<Integer> stages = renewalProperties.reminderDays().stream().sorted().toList();

        // Every licence except the ones already RENEWED
        List<SoftwareLicense> licenses = softwareLicenseRepository.findByStatusNot(LicenseStatus.RENEWED);

        for (SoftwareLicense license : licenses) {
            checked++;

            // ---- 1. Keep the status up to date ----
            LicenseStatus newStatus = expiryCalculator.calculateStatus(license);
            if (newStatus != license.getStatus()) {
                license.setStatus(newStatus);
                softwareLicenseRepository.save(license);
                statusesUpdated++;
            }

            // ---- 2. Which reminder stage is this licence in? ----
            Long daysRemaining = expiryCalculator.daysRemaining(license.getExpiryDate());
            Integer stage = findStage(daysRemaining, stages);
            if (stage == null) {
                continue;   // too far away, already expired, or no expiry date
            }

            // Renewal already finished (RENEWED / LOST) -> no more reminders
            if (renewalRepository.existsByLicenseIdAndStatusIn(license.getId(), CLOSED_RENEWAL)) {
                continue;
            }

            // ---- 3. Find the open renewal, or create one ----
            Renewal renewal = renewalRepository
                    .findFirstByLicenseIdAndStatusInOrderByIdDesc(license.getId(), OPEN_RENEWAL)
                    .orElse(null);

            if (renewal == null) {
                renewal = createRenewal(license);
                renewalsCreated++;
            }

            // ---- 4. Duplicate check: this stage (or a later one) already handled? ----
            if (renewal.getLastReminderDays() != null && renewal.getLastReminderDays() <= stage) {
                skipped++;
                continue;
            }

            // ---- 5. Create the task + notifications for this stage ----
            cancelOlderOpenTasks(renewal);
            createSalesTask(license, renewal, daysRemaining, stage);
            tasksCreated++;

            notificationsCreated += createNotifications(license, renewal, daysRemaining);

            renewal.setLastReminderDays(stage);
            renewalRepository.save(renewal);
        }

        RenewalAutomationResultDto result = new RenewalAutomationResultDto(
                LocalDateTime.now(), checked, statusesUpdated, renewalsCreated,
                tasksCreated, notificationsCreated, skipped);

        log.info("Renewal check finished: {}", result);
        return result;
    }

    /**
     * Returns the smallest reminder stage that the licence has reached.
     * Example with stages [7, 14, 30, 60, 90]:
     *   100 days left -> null (no reminder yet)
     *    58 days left -> 60
     *    30 days left -> 30
     *     3 days left -> 7
     *    -2 days left -> null (expired, handled by status EXPIRED)
     * Using "reached" instead of "exactly equal" means a reminder is not missed
     * if the scheduler did not run on a particular day.
     */
    private Integer findStage(Long daysRemaining, List<Integer> sortedStages) {
        if (daysRemaining == null || daysRemaining < 0) {
            return null;
        }
        for (Integer stage : sortedStages) {
            if (daysRemaining <= stage) {
                return stage;
            }
        }
        return null;
    }

    private Renewal createRenewal(SoftwareLicense license) {
        Renewal renewal = new Renewal();
        renewal.setLicense(license);
        renewal.setCustomer(license.getCustomer());
        renewal.setStatus(RenewalStatus.PENDING);
        renewal.setNextRenewalDate(license.getExpiryDate());
        renewal.setNotes("Created automatically by the renewal scheduler");
        return renewalRepository.save(renewal);
    }

    /** Older reminder tasks that nobody started are replaced by the new, more urgent one. */
    private void cancelOlderOpenTasks(Renewal renewal) {
        List<SalesTask> oldTasks = salesTaskRepository.findByRenewalIdAndTaskStatusIn(
                renewal.getId(), List.of(TaskStatus.OPEN));
        oldTasks.forEach(task -> task.setTaskStatus(TaskStatus.CANCELLED));
        salesTaskRepository.saveAll(oldTasks);
    }

    private void createSalesTask(SoftwareLicense license, Renewal renewal, long daysRemaining, int stage) {

        String customerName = customerName(license.getCustomer());
        String productName = license.getProduct() != null ? license.getProduct().getProductName() : "software";

        // Due in N days, but never after the expiry date
        LocalDate dueDate = LocalDate.now().plusDays(renewalProperties.taskDueInDays());
        LocalDate expiry = license.getExpiryDate().toLocalDate();
        if (dueDate.isAfter(expiry)) {
            dueDate = expiry;
        }

        SalesTask task = new SalesTask();
        task.setTitle("Renewal: " + customerName + " - " + productName + " expires in " + daysRemaining + " days");
        task.setDescription(
                "Customer: " + customerName + "\n" +
                "Company: " + companyName(license.getCustomer()) + "\n" +
                "Software: " + productName + "\n" +
                "Licences: " + license.getLicenseQuantity() + "\n" +
                "Duration: " + license.getDurationMonths() + " months\n" +
                "Expiry date: " + expiry + "\n" +
                "Reminder stage: " + stage + " days\n" +
                "Action: contact the customer about renewal (quantity + duration).");
        task.setTaskType("RENEWAL");
        task.setPriority(priorityFor(daysRemaining));
        task.setTaskStatus(TaskStatus.OPEN);
        task.setDueDate(dueDate);
        task.setCustomer(license.getCustomer());
        task.setLicense(license);
        task.setRenewal(renewal);
        task.setSalesperson(license.getSalesperson());

        salesTaskRepository.save(task);
    }

    private TaskPriority priorityFor(long daysRemaining) {
        if (daysRemaining <= 7) return TaskPriority.URGENT;
        if (daysRemaining <= 14) return TaskPriority.HIGH;
        if (daysRemaining <= 30) return TaskPriority.MEDIUM;
        return TaskPriority.LOW;
    }

    /**
     * Creates the "to send" notifications. They are saved with status PENDING;
     * actually sending email/SMS comes in a later phase.
     */
    private int createNotifications(SoftwareLicense license, Renewal renewal, long daysRemaining) {

        int created = 0;
        String productName = license.getProduct() != null ? license.getProduct().getProductName() : "software";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        // Internal – for the salesperson
        SalesPerson salesperson = license.getSalesperson();
        Notification internal = new Notification();
        internal.setType("RENEWAL_REMINDER_INTERNAL");
        internal.setMessage(customerName(license.getCustomer()) + "'s " + productName + " licence ("
                + license.getLicenseQuantity() + " licences) expires in " + daysRemaining
                + " days on " + license.getExpiryDate().toLocalDate() + ". A renewal task has been created.");
        internal.setStatus(NotificationStatus.PENDING);
        internal.setScheduledAt(now);
        internal.setSalesPerson(salesperson);
        internal.setCustomer(license.getCustomer());
        internal.setLicense(license);
        internal.setRenewal(renewal);
        notificationRepository.save(internal);
        created++;

        // Customer reminder
        if (license.getCustomer() != null) {
            Notification customerReminder = new Notification();
            customerReminder.setType("RENEWAL_REMINDER_CUSTOMER");
            customerReminder.setMessage("Hello " + customerName(license.getCustomer()) + ", your " + productName
                    + " licence expires on " + license.getExpiryDate().toLocalDate()
                    + " (" + daysRemaining + " days). Our team will contact you about renewal.");
            customerReminder.setStatus(NotificationStatus.PENDING);
            customerReminder.setScheduledAt(now);
            customerReminder.setCustomer(license.getCustomer());
            customerReminder.setSalesPerson(salesperson);
            customerReminder.setLicense(license);
            customerReminder.setRenewal(renewal);
            notificationRepository.save(customerReminder);
            created++;
        }

        return created;
    }

    private String customerName(Customer customer) {
        return customer != null ? customer.getCustomerName() : "Unknown customer";
    }

    private String companyName(Customer customer) {
        return customer != null ? customer.getCompanyName() : "-";
    }
}
