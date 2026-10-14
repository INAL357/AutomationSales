package com.app.AutomationSales.notification.service;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.customer.repository.CustomerRepository;
import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.lead.repository.LeadRepository;
import com.app.AutomationSales.notification.dto.NotificationRequestDto;
import com.app.AutomationSales.notification.dto.NotificationResponseDto;
import com.app.AutomationSales.notification.model.Notification;
import com.app.AutomationSales.notification.model.NotificationStatus;
import com.app.AutomationSales.notification.repository.NotificationRepository;
import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.renewal.repository.RenewalRepository;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salesperson.repository.SalesPersonRepository;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.softwarelicense.repository.SoftwareLicenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final RenewalRepository renewalRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            CustomerRepository customerRepository,
            LeadRepository leadRepository,
            SalesPersonRepository salesPersonRepository,
            SoftwareLicenseRepository softwareLicenseRepository,
            RenewalRepository renewalRepository) {
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.softwareLicenseRepository = softwareLicenseRepository;
        this.renewalRepository = renewalRepository;
    }

    @Override
    public NotificationResponseDto createNotification(NotificationRequestDto request) {

        Notification notification = new Notification();
        applyRequest(notification, request);

        Notification savedNotification = notificationRepository.save(notification);

        return toResponse(savedNotification);
    }

    @Override
    public List<NotificationResponseDto> findAll() {
        return toResponseList(notificationRepository.findAll());
    }

    @Override
    public NotificationResponseDto findViewById(Long id) {
        return toResponse(findNotificationEntityById(id));
    }

    @Override
    public NotificationResponseDto update(NotificationRequestDto request, Long id) {

        Notification existingNotification = findNotificationEntityById(id);
        applyRequest(existingNotification, request);

        Notification updatedNotification = notificationRepository.save(existingNotification);

        return toResponse(updatedNotification);
    }

    @Override
    public void delete(Long id) {
        Notification existingNotification = findNotificationEntityById(id);
        notificationRepository.delete(existingNotification);
    }

    @Override
    public List<NotificationResponseDto> findBySalesPersonId(Long salesPersonId) {
        return toResponseList(notificationRepository.findBySalesPersonId(salesPersonId));
    }

    @Override
    public List<NotificationResponseDto> findByCustomerId(Long customerId) {
        return toResponseList(notificationRepository.findByCustomerId(customerId));
    }

    @Override
    public List<NotificationResponseDto> findByLicenseId(Long licenseId) {
        return toResponseList(notificationRepository.findByLicenseId(licenseId));
    }

    @Override
    public List<NotificationResponseDto> findByRenewalId(Long renewalId) {
        return toResponseList(notificationRepository.findByRenewalId(renewalId));
    }

    @Override
    public List<NotificationResponseDto> findByLeadId(Long leadId) {
        return toResponseList(notificationRepository.findByLeadId(leadId));
    }

    /* ---------- Helpers ---------- */

    private Notification findNotificationEntityById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Notification not found with id: " + id));
    }

    private void applyRequest(Notification notification, NotificationRequestDto request) {
        notification.setType(request.type());
        notification.setRecipientEmail(request.recipientEmail());
        notification.setSubject(request.subject());
        notification.setMessage(request.message());
        if (request.status() != null) {
            notification.setStatus(request.status());
        } else if (notification.getStatus() == null) {
            notification.setStatus(NotificationStatus.PENDING);
        }
        notification.setScheduledAt(request.scheduledAt());
        notification.setSentAt(request.sentAt());

        notification.setCustomer(findCustomer(request.customerId()));
        notification.setLead(findLead(request.leadId()));
        notification.setSalesPerson(findSalesperson(request.salesPersonId()));
        notification.setLicense(findLicense(request.licenseId()));
        notification.setRenewal(findRenewal(request.renewalId()));
    }

    private Customer findCustomer(Long customerId) {
        if (customerId == null) return null;
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Customer not found with id: " + customerId));
    }

    private Lead findLead(Long leadId) {
        if (leadId == null) return null;
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Lead not found with id: " + leadId));
    }

    private SalesPerson findSalesperson(Long salesPersonId) {
        if (salesPersonId == null) return null;
        return salesPersonRepository.findById(salesPersonId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesPerson not found with id: " + salesPersonId));
    }

    private SoftwareLicense findLicense(Long licenseId) {
        if (licenseId == null) return null;
        return softwareLicenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareLicense not found with id: " + licenseId));
    }

    private Renewal findRenewal(Long renewalId) {
        if (renewalId == null) return null;
        return renewalRepository.findById(renewalId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Renewal not found with id: " + renewalId));
    }

    private List<NotificationResponseDto> toResponseList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }
    private NotificationResponseDto toResponse(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getCustomer() != null ? notification.getCustomer().getId() : null,
                notification.getLead() != null ? notification.getLead().getId() : null,
                notification.getSalesPerson() != null ? notification.getSalesPerson().getId() : null,
                notification.getLicense() != null ? notification.getLicense().getId() : null,
                notification.getRenewal() != null ? notification.getRenewal().getId() : null,
                notification.getType(),
                notification.getRecipientEmail(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getScheduledAt(),
                notification.getSentAt(),
                notification.getFailureReason(),
                notification.getCreatedAt(),
                notification.getModifiedAt()
        );
    }
}
