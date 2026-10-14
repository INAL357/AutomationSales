package com.app.AutomationSales.security;

import com.app.AutomationSales.appUser.model.Role;
import com.app.AutomationSales.customer.repository.CustomerRepository;
import com.app.AutomationSales.lead.repository.LeadRepository;
import com.app.AutomationSales.notification.repository.NotificationRepository;
import com.app.AutomationSales.renewal.repository.RenewalRepository;
import com.app.AutomationSales.salescall.repository.SalesCallRepository;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salestask.repository.SalesTaskRepository;
import com.app.AutomationSales.softwarelicense.repository.SoftwareLicenseRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * "Is the logged-in user allowed to touch this record?"
 *
 * Used from controllers like this:
 *     @PreAuthorize("@access.canUseLead(#id)")
 *
 * "@access" is this class (see the name in @Component), "#id" is the method parameter.
 * If a method returns false, Spring answers 403 Forbidden and the controller method never runs.
 *
 * Rules:
 *   - ADMIN can use everything.
 *   - SALESPERSON can only use records whose salesperson is THEM.
 *   - If the record does not exist we return true, so the service can answer the normal 404.
 */
@Component("access")
public class AccessGuard {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final SoftwareLicenseRepository licenseRepository;
    private final RenewalRepository renewalRepository;
    private final SalesTaskRepository taskRepository;
    private final SalesCallRepository callRepository;
    private final NotificationRepository notificationRepository;

    public AccessGuard(LeadRepository leadRepository,
                       CustomerRepository customerRepository,
                       SoftwareLicenseRepository licenseRepository,
                       RenewalRepository renewalRepository,
                       SalesTaskRepository taskRepository,
                       SalesCallRepository callRepository,
                       NotificationRepository notificationRepository) {
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
        this.licenseRepository = licenseRepository;
        this.renewalRepository = renewalRepository;
        this.taskRepository = taskRepository;
        this.callRepository = callRepository;
        this.notificationRepository = notificationRepository;
    }

    /* ---------- Who is logged in? ---------- */

    private SecurityUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser user) {
            return user;
        }
        return null;
    }

    public boolean isAdmin() {
        SecurityUser user = currentUser();
        return user != null && user.getRole() == Role.ADMIN;
    }

    /** True if this salesPersonId is the logged-in salesperson (admins: always true). */
    public boolean isMe(Long salesPersonId) {
        if (isAdmin()) return true;
        SecurityUser user = currentUser();
        return user != null && salesPersonId != null && salesPersonId.equals(user.getSalesPersonId());
    }

    /**
     * Creating / editing a record: a salesperson may only assign it to THEMSELVES.
     * (Stops a salesperson from creating a lead "for" someone else.)
     */
    public boolean canAssignTo(Long salesPersonId) {
        return isMe(salesPersonId);
    }

    /* ---------- One record by id ---------- */

    @Transactional(readOnly = true)
    public boolean canUseLead(Long leadId) {
        if (isAdmin()) return true;
        if (leadId == null) return false;
        return leadRepository.findById(leadId)
                .map(lead -> isOwner(lead.getSalesperson()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean canUseCustomer(Long customerId) {
        if (isAdmin()) return true;
        if (customerId == null) return false;
        return customerRepository.findById(customerId)
                .map(customer -> isOwner(customer.getSalesperson()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean canUseLicense(Long licenseId) {
        if (isAdmin()) return true;
        if (licenseId == null) return false;
        return licenseRepository.findById(licenseId)
                .map(license -> isOwner(license.getSalesperson()))
                .orElse(true);
    }

    /** A renewal belongs to the salesperson of its licence. */
    @Transactional(readOnly = true)
    public boolean canUseRenewal(Long renewalId) {
        if (isAdmin()) return true;
        if (renewalId == null) return false;
        return renewalRepository.findById(renewalId)
                .map(renewal -> renewal.getLicense() != null && isOwner(renewal.getLicense().getSalesperson()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean canUseTask(Long taskId) {
        if (isAdmin()) return true;
        if (taskId == null) return false;
        return taskRepository.findById(taskId)
                .map(task -> isOwner(task.getSalesperson()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean canUseCall(Long callId) {
        if (isAdmin()) return true;
        if (callId == null) return false;
        return callRepository.findById(callId)
                .map(call -> isOwner(call.getSalesperson()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean canUseNotification(Long notificationId) {
        if (isAdmin()) return true;
        if (notificationId == null) return false;
        return notificationRepository.findById(notificationId)
                .map(notification -> isOwner(notification.getSalesPerson()))
                .orElse(true);
    }

    /* ---------- helper ---------- */

    private boolean isOwner(SalesPerson owner) {
        SecurityUser user = currentUser();
        if (owner == null || user == null || user.getSalesPersonId() == null) {
            return false;   // unassigned records: only admins
        }
        return Objects.equals(owner.getId(), user.getSalesPersonId());
    }
}
