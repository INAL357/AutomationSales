package com.app.AutomationSales.renewal.service;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.customer.repository.CustomerRepository;
import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.renewal.dto.RenewalRequestDto;
import com.app.AutomationSales.renewal.dto.RenewalResponseDto;
import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.renewal.model.RenewalStatus;
import com.app.AutomationSales.renewal.repository.RenewalRepository;
import com.app.AutomationSales.salestask.model.SalesTask;
import com.app.AutomationSales.salestask.model.TaskStatus;
import com.app.AutomationSales.salestask.repository.SalesTaskRepository;
import com.app.AutomationSales.softwarelicense.model.LicenseStatus;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.softwarelicense.repository.SoftwareLicenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RenewalServiceImpl implements RenewalService {

    private final RenewalRepository renewalRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final CustomerRepository customerRepository;
    private final SalesTaskRepository salesTaskRepository;

    public RenewalServiceImpl(
            RenewalRepository renewalRepository,
            SoftwareLicenseRepository softwareLicenseRepository,
            CustomerRepository customerRepository,
            SalesTaskRepository salesTaskRepository) {
        this.renewalRepository = renewalRepository;
        this.softwareLicenseRepository = softwareLicenseRepository;
        this.customerRepository = customerRepository;
        this.salesTaskRepository = salesTaskRepository;
    }

    @Override
    @Transactional
    public RenewalResponseDto createRenewal(RenewalRequestDto request) {

        Renewal renewal = new Renewal();
        applyRequest(renewal, request);

        Renewal savedRenewal = renewalRepository.save(renewal);
        handleClosedRenewal(savedRenewal);

        return toResponse(savedRenewal);
    }

    @Override
    public List<RenewalResponseDto> renewalList() {
        return toResponseList(renewalRepository.findAll());
    }

    @Override
    public RenewalResponseDto findById(Long id) {
        return toResponse(findRenewalEntityById(id));
    }

    @Override
    @Transactional
    public RenewalResponseDto updateRenewal(RenewalRequestDto request, Long id) {

        Renewal existingRenewal = findRenewalEntityById(id);
        applyRequest(existingRenewal, request);

        Renewal updatedRenewal = renewalRepository.save(existingRenewal);
        handleClosedRenewal(updatedRenewal);

        return toResponse(updatedRenewal);
    }

    @Override
    @Transactional
    public RenewalResponseDto updateRenewalStatus(Long id, RenewalStatus status) {

        Renewal existingRenewal = findRenewalEntityById(id);
        existingRenewal.setStatus(status);

        Renewal updatedRenewal = renewalRepository.save(existingRenewal);
        handleClosedRenewal(updatedRenewal);

        return toResponse(updatedRenewal);
    }

    @Override
    public void deleteById(Long id) {
        Renewal existingRenewal = findRenewalEntityById(id);
        renewalRepository.delete(existingRenewal);
    }

    @Override
    public List<RenewalResponseDto> findByLicenseId(Long licenseId) {
        return toResponseList(renewalRepository.findByLicenseId(licenseId));
    }

    @Override
    public List<RenewalResponseDto> findByStatus(RenewalStatus status) {
        return toResponseList(renewalRepository.findByStatus(status));
    }

    @Override
    public List<RenewalResponseDto> findByCustomerId(Long customerId) {
        return toResponseList(renewalRepository.findByCustomerId(customerId));
    }

    @Override
    public List<RenewalResponseDto> findBySalespersonId(Long salesPersonId) {
        return toResponseList(renewalRepository.findByLicense_Salesperson_Id(salesPersonId));
    }

    /* ---------- Business rules ---------- */

    /**
     * When a renewal is finished:
     *  - RENEWED -> the licence is marked RENEWED (the scheduler ignores it from now on)
     *  - RENEWED or LOST -> any OPEN / IN_PROGRESS tasks for this renewal are CANCELLED
     */
    private void handleClosedRenewal(Renewal renewal) {

        RenewalStatus status = renewal.getStatus();
        if (status != RenewalStatus.RENEWED && status != RenewalStatus.LOST) {
            return;
        }

        if (status == RenewalStatus.RENEWED && renewal.getLicense() != null) {
            SoftwareLicense license = renewal.getLicense();
            license.setStatus(LicenseStatus.RENEWED);
            softwareLicenseRepository.save(license);
        }

        List<SalesTask> openTasks = salesTaskRepository.findByRenewalIdAndTaskStatusIn(
                renewal.getId(), List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS));

        openTasks.forEach(task -> task.setTaskStatus(TaskStatus.CANCELLED));
        salesTaskRepository.saveAll(openTasks);
    }

    /* ---------- Helpers ---------- */

    private Renewal findRenewalEntityById(Long id) {
        return renewalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Renewal not found with id: " + id));
    }

    private void applyRequest(Renewal renewal, RenewalRequestDto request) {
        renewal.setStatus(request.status() != null ? request.status() : RenewalStatus.PENDING);
        renewal.setNextRenewalDate(request.nextRenewalDate());
        renewal.setCustomerIntent(request.customerIntent());
        renewal.setRequestedLicenseCount(request.requestedLicenseCount());
        renewal.setRequestedDurationMonths(request.requestedDurationMonths());
        renewal.setNotes(request.notes());

        SoftwareLicense license = softwareLicenseRepository.findById(request.licenseId())
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareLicense not found with id: " + request.licenseId()));
        renewal.setLicense(license);

        // If no customerId is sent, use the licence's customer
        if (request.customerId() == null) {
            renewal.setCustomer(license.getCustomer());
        } else {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                            "Customer not found with id: " + request.customerId()));
            renewal.setCustomer(customer);
        }

        // Default the next renewal date to the licence expiry date
        if (renewal.getNextRenewalDate() == null) {
            renewal.setNextRenewalDate(license.getExpiryDate());
        }
    }

    private List<RenewalResponseDto> toResponseList(List<Renewal> renewals) {
        return renewals.stream()
                .map(this::toResponse)
                .toList();
    }

    private RenewalResponseDto toResponse(Renewal renewal) {

        Long licenseId = renewal.getLicense() != null
                ? renewal.getLicense().getId()
                : null;

        Long customerId = renewal.getCustomer() != null
                ? renewal.getCustomer().getId()
                : null;

        return new RenewalResponseDto(
                renewal.getId(),
                licenseId,
                customerId,
                renewal.getStatus(),
                renewal.getNextRenewalDate(),
                renewal.getCustomerIntent(),
                renewal.getRequestedLicenseCount(),
                renewal.getRequestedDurationMonths(),
                renewal.getNotes(),
                renewal.getLastReminderDays(),
                renewal.getCreatedAt(),
                renewal.getModifiedAt()
        );
    }
}
