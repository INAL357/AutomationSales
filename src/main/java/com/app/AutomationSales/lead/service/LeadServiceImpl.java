package com.app.AutomationSales.lead.service;

import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.lead.dto.LeadRequestDto;
import com.app.AutomationSales.lead.dto.LeadResponseDto;
import com.app.AutomationSales.lead.dto.LeadStatusUpdateRequestDto;
import com.app.AutomationSales.lead.dto.PublicLeadRequestDto;
import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.lead.model.LeadStatus;
import com.app.AutomationSales.lead.repository.LeadRepository;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salesperson.repository.SalesPersonRepository;
import com.app.AutomationSales.salestask.dto.SalesTaskRequestDto;
import com.app.AutomationSales.salestask.model.TaskPriority;
import com.app.AutomationSales.salestask.model.TaskStatus;
import com.app.AutomationSales.salestask.service.SalesTaskService;
import com.app.AutomationSales.softwareproduct.model.SoftwareProduct;
import com.app.AutomationSales.softwareproduct.repository.SoftwareProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final SoftwareProductRepository softwareProductRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final SalesTaskService salesTaskService;

    public LeadServiceImpl(
            LeadRepository leadRepository,
            SoftwareProductRepository softwareProductRepository,
            SalesPersonRepository salesPersonRepository,
            SalesTaskService salesTaskService) {

        this.leadRepository = leadRepository;
        this.softwareProductRepository = softwareProductRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.salesTaskService = salesTaskService;
    }

    @Override
    public LeadResponseDto createLead(LeadRequestDto request) {

        Lead lead = new Lead();
        applyRequest(lead, request);

        Lead savedLead = leadRepository.save(lead);

        return toResponse(savedLead);
    }

    @Transactional
    @Override
    public LeadResponseDto createWebsiteLead(PublicLeadRequestDto request) {

        SoftwareProduct product = softwareProductRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareProduct not found with id: " + request.productId()));

        SalesPerson salesperson = findSalespersonForNewLead();

        Lead lead = new Lead();
        lead.setLeadName(request.leadName());
        lead.setCompanyName(request.companyName());
        lead.setEmail(request.email());
        lead.setPhone(request.phone());
        lead.setProduct(product);
        lead.setSalesperson(salesperson);
        lead.setSource("WEBSITE");
        lead.setMessage(request.message());
        lead.setStatus(LeadStatus.NEW);
        lead.setLeadScore(null);

        Lead savedLead = leadRepository.save(lead);

        createWebsiteLeadFollowUpTask(savedLead, salesperson);

        return toResponse(savedLead);
    }

    @Override
    public List<LeadResponseDto> findAllLeads() {
        return leadRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LeadResponseDto findLeadById(Long id) {
        return toResponse(findLeadEntityById(id));
    }

    @Override
    public LeadResponseDto updateLead(LeadRequestDto request, Long id) {

        Lead existingLead = findLeadEntityById(id);
        applyRequest(existingLead, request);

        Lead updatedLead = leadRepository.save(existingLead);

        return toResponse(updatedLead);
    }

    @Override
    public LeadResponseDto updateLeadStatus(Long id, LeadStatusUpdateRequestDto request) {

        Lead lead = findLeadEntityById(id);
        lead.setStatus(request.status());

        return toResponse(leadRepository.save(lead));
    }

    @Override
    public void deleteLeadById(Long id) {
        Lead existingLead = findLeadEntityById(id);
        leadRepository.delete(existingLead);
    }

    @Override
    public List<LeadResponseDto> findBySalespersonId(Long salesPersonId) {
        return leadRepository.findBySalespersonId(salesPersonId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LeadResponseDto> findByProductId(Long productId) {
        return leadRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LeadResponseDto> findByCompanyName(String companyName) {
        return leadRepository.findByCompanyName(companyName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LeadResponseDto> findByStatus(LeadStatus status) {
        return leadRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Lead findLeadEntityById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Lead not found with id: " + id));
    }

    private void applyRequest(Lead lead, LeadRequestDto request) {
        lead.setLeadName(request.leadName());
        lead.setCompanyName(request.companyName());
        lead.setEmail(request.email());
        lead.setPhone(request.phone());

        if (request.source() != null) {
            lead.setSource(request.source());
        } else if (lead.getSource() == null) {
            lead.setSource("WEBSITE");
        }

        lead.setMessage(request.message());

        if (request.status() != null) {
            lead.setStatus(request.status());
        } else if (lead.getStatus() == null) {
            lead.setStatus(LeadStatus.NEW);
        }

        lead.setLeadScore(request.leadScore());
        lead.setRequiredLicenses(request.requiredLicenses());
        lead.setExistingLicenses(request.existingLicenses());
        lead.setPreferredDurationMonths(request.preferredDurationMonths());
        lead.setPurchaseTimeline(request.purchaseTimeline());

        setProduct(lead, request.productId());
        setSalesperson(lead, request.salespersonId());
    }

    private SalesPerson findSalespersonForNewLead() {

        List<SalesPerson> activeSalespeople = salesPersonRepository.findByStatus("ACTIVE");

        if (activeSalespeople.isEmpty()) {
            throw new ResourceNotFoundExceptionHandler("No ACTIVE salesperson available for new lead");
        }

        // Assign the active salesperson who currently has the fewest leads.
        return activeSalespeople.stream()
                .min(Comparator.comparingLong(
                        salesPerson -> leadRepository.countBySalespersonId(salesPerson.getId())))
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "No ACTIVE salesperson available for new lead"));
    }

    private void createWebsiteLeadFollowUpTask(Lead lead, SalesPerson salesperson) {

        SalesTaskRequestDto taskRequest = new SalesTaskRequestDto(
                null,                                   // customerId
                lead.getId(),                           // leadId
                null,                                   // licenseId
                null,                                   // renewalId
                salesperson.getId(),                    // salespersonId
                "Follow up website lead",               // title (required, max 200)
                "Contact website lead, confirm requirements and qualify the opportunity", // description
                "LEAD_FOLLOW_UP",                       // taskType
                TaskPriority.MEDIUM,                    // priority
                TaskStatus. IN_PROGRESS,
                LocalDate.now().plusDays(1)             // dueDate
        );


        salesTaskService.createSalesTask(taskRequest);
    }

    private void setProduct(Lead lead, Long productId) {
        if (productId == null) {
            lead.setProduct(null);
            return;
        }

        SoftwareProduct product = softwareProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareProduct not found with id: " + productId));

        lead.setProduct(product);
    }

    private void setSalesperson(Lead lead, Long salespersonId) {
        if (salespersonId == null) {
            lead.setSalesperson(null);
            return;
        }

        SalesPerson salesperson = salesPersonRepository.findById(salespersonId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesPerson not found with id: " + salespersonId));

        lead.setSalesperson(salesperson);
    }

    private LeadResponseDto toResponse(Lead lead) {

        Long productId = lead.getProduct() != null
                ? lead.getProduct().getId()
                : null;

        Long salespersonId = lead.getSalesperson() != null
                ? lead.getSalesperson().getId()
                : null;

        return new LeadResponseDto(
                lead.getId(),
                lead.getLeadName(),
                productId,
                salespersonId,
                lead.getCompanyName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getSource(),
                lead.getMessage(),
                lead.getStatus(),
                lead.getLeadScore(),
                lead.getRequiredLicenses(),
                lead.getExistingLicenses(),
                lead.getPreferredDurationMonths(),
                lead.getPurchaseTimeline(),
                lead.getCreatedAt(),
                lead.getModifiedAt()
        );
    }
}
