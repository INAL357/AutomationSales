package com.app.AutomationSales.salescall.service;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.customer.repository.CustomerRepository;
import com.app.AutomationSales.exceptionHandling.BadRequestException;
import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.lead.repository.LeadRepository;
import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.renewal.repository.RenewalRepository;
import com.app.AutomationSales.salescall.dto.SalesCallRequestDto;
import com.app.AutomationSales.salescall.dto.SalesCallResponseDto;
import com.app.AutomationSales.salescall.model.SalesCall;
import com.app.AutomationSales.salescall.repository.SalesCallRepository;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salesperson.repository.SalesPersonRepository;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.softwarelicense.repository.SoftwareLicenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SalesCallServiceImpl implements SalesCallService {

    private final SalesCallRepository salesCallRepository;
    private final CustomerRepository customerRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final LeadRepository leadRepository;
    private final RenewalRepository renewalRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;

    public SalesCallServiceImpl(
            SalesCallRepository salesCallRepository,
            CustomerRepository customerRepository,
            SalesPersonRepository salesPersonRepository,
            LeadRepository leadRepository,
            RenewalRepository renewalRepository,
            SoftwareLicenseRepository softwareLicenseRepository) {
        this.salesCallRepository = salesCallRepository;
        this.customerRepository = customerRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.leadRepository = leadRepository;
        this.renewalRepository = renewalRepository;
        this.softwareLicenseRepository = softwareLicenseRepository;
    }

    @Override
    public SalesCallResponseDto createSalesCall(SalesCallRequestDto request) {

        SalesCall salesCall = new SalesCall();
        applyRequest(salesCall, request);

        SalesCall savedSalesCall = salesCallRepository.save(salesCall);

        return toResponse(savedSalesCall);
    }

    @Override
    public List<SalesCallResponseDto> findAll() {
        return toResponseList(salesCallRepository.findAll());
    }

    @Override
    public SalesCallResponseDto findById(Long id) {
        return toResponse(findSalesCallEntityById(id));
    }

    @Override
    public SalesCallResponseDto updateSalesCall(SalesCallRequestDto request, Long id) {

        SalesCall existingSalesCall = findSalesCallEntityById(id);
        applyRequest(existingSalesCall, request);

        SalesCall updatedSalesCall = salesCallRepository.save(existingSalesCall);

        return toResponse(updatedSalesCall);
    }

    @Override
    public void deleteSalesCallById(Long id) {
        SalesCall existingSalesCall = findSalesCallEntityById(id);
        salesCallRepository.delete(existingSalesCall);
    }

    @Override
    public List<SalesCallResponseDto> findBySalespersonId(Long salespersonId) {
        return toResponseList(salesCallRepository.findBySalespersonId(salespersonId));
    }

    @Override
    public List<SalesCallResponseDto> findByCustomerId(Long customerId) {
        return toResponseList(salesCallRepository.findByCustomerId(customerId));
    }

    @Override
    public List<SalesCallResponseDto> findByLeadId(Long leadId) {
        return toResponseList(salesCallRepository.findByLeadId(leadId));
    }

    @Override
    public List<SalesCallResponseDto> findByRenewalId(Long renewalId) {
        return toResponseList(salesCallRepository.findByRenewalId(renewalId));
    }

    @Override
    public List<SalesCallResponseDto> findByLicenseId(Long licenseId) {
        return toResponseList(salesCallRepository.findByLicenseId(licenseId));
    }

    @Override
    public List<SalesCallResponseDto> findByStartTimeBetween(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' must be before 'to'");
        }
        return toResponseList(salesCallRepository.findByStartTimeBetween(from, to));
    }

    /* ---------- Helpers ---------- */

    private SalesCall findSalesCallEntityById(Long id) {
        return salesCallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesCall not found with id: " + id));
    }

    private void applyRequest(SalesCall salesCall, SalesCallRequestDto request) {

        if (request.customerId() == null && request.leadId() == null) {
            throw new BadRequestException("A call must be linked to a customerId or a leadId");
        }
        if (request.startTime() != null && request.endTime() != null
                && request.endTime().isBefore(request.startTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }

        salesCall.setCallType(request.callType());
        salesCall.setCallStatus(request.callStatus());
        salesCall.setStartTime(request.startTime());
        salesCall.setEndTime(request.endTime());
        salesCall.setTranscript(request.transcript());
        salesCall.setCallSummary(request.callSummary());
        salesCall.setOutcome(request.outcome());
        salesCall.setNextAction(request.nextAction());

        salesCall.setCustomer(findCustomer(request.customerId()));
        salesCall.setSalesperson(findSalesperson(request.salespersonId()));
        salesCall.setLead(findLead(request.leadId()));
        salesCall.setRenewal(findRenewal(request.renewalId()));
        salesCall.setLicense(findLicense(request.licenseId()));
    }

    private Customer findCustomer(Long customerId) {
        if (customerId == null) return null;
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Customer not found with id: " + customerId));
    }

    private SalesPerson findSalesperson(Long salespersonId) {
        if (salespersonId == null) return null;
        return salesPersonRepository.findById(salespersonId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesPerson not found with id: " + salespersonId));
    }

    private Lead findLead(Long leadId) {
        if (leadId == null) return null;
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Lead not found with id: " + leadId));
    }

    private Renewal findRenewal(Long renewalId) {
        if (renewalId == null) return null;
        return renewalRepository.findById(renewalId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Renewal not found with id: " + renewalId));
    }

    private SoftwareLicense findLicense(Long licenseId) {
        if (licenseId == null) return null;
        return softwareLicenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareLicense not found with id: " + licenseId));
    }

    private List<SalesCallResponseDto> toResponseList(List<SalesCall> salesCalls) {
        return salesCalls.stream()
                .map(this::toResponse)
                .toList();
    }

    private SalesCallResponseDto toResponse(SalesCall salesCall) {

        Long durationMinutes = (salesCall.getStartTime() != null && salesCall.getEndTime() != null)
                ? ChronoUnit.MINUTES.between(salesCall.getStartTime(), salesCall.getEndTime())
                : null;

        return new SalesCallResponseDto(
                salesCall.getId(),
                salesCall.getCustomer() != null ? salesCall.getCustomer().getId() : null,
                salesCall.getSalesperson() != null ? salesCall.getSalesperson().getId() : null,
                salesCall.getLead() != null ? salesCall.getLead().getId() : null,
                salesCall.getRenewal() != null ? salesCall.getRenewal().getId() : null,
                salesCall.getLicense() != null ? salesCall.getLicense().getId() : null,
                salesCall.getCallType(),
                salesCall.getCallStatus(),
                salesCall.getStartTime(),
                salesCall.getEndTime(),
                durationMinutes,
                salesCall.getTranscript(),
                salesCall.getCallSummary(),
                salesCall.getOutcome(),
                salesCall.getNextAction(),
                salesCall.getCreatedAt(),
                salesCall.getModifiedAt()
        );
    }
}
