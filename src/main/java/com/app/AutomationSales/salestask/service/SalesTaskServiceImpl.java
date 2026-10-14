package com.app.AutomationSales.salestask.service;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.customer.repository.CustomerRepository;
import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.lead.repository.LeadRepository;
import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.renewal.repository.RenewalRepository;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salesperson.repository.SalesPersonRepository;
import com.app.AutomationSales.salestask.dto.SalesTaskRequestDto;
import com.app.AutomationSales.salestask.dto.SalesTaskResponseDto;
import com.app.AutomationSales.salestask.model.SalesTask;
import com.app.AutomationSales.salestask.model.TaskPriority;
import com.app.AutomationSales.salestask.model.TaskStatus;
import com.app.AutomationSales.salestask.repository.SalesTaskRepository;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.softwarelicense.repository.SoftwareLicenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SalesTaskServiceImpl implements SalesTaskService {

    private static final List<TaskStatus> OPEN_STATUSES = List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS);

    private final SalesTaskRepository salesTaskRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final RenewalRepository renewalRepository;
    private final SalesPersonRepository salesPersonRepository;

    public SalesTaskServiceImpl(
            SalesTaskRepository salesTaskRepository,
            CustomerRepository customerRepository,
            LeadRepository leadRepository,
            SoftwareLicenseRepository softwareLicenseRepository,
            RenewalRepository renewalRepository,
            SalesPersonRepository salesPersonRepository) {
        this.salesTaskRepository = salesTaskRepository;
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.softwareLicenseRepository = softwareLicenseRepository;
        this.renewalRepository = renewalRepository;
        this.salesPersonRepository = salesPersonRepository;
    }

    @Override
    public SalesTaskResponseDto createSalesTask(SalesTaskRequestDto request) {

        SalesTask salesTask = new SalesTask();
        applyRequest(salesTask, request);

        SalesTask savedSalesTask = salesTaskRepository.save(salesTask);

        return toResponse(savedSalesTask);
    }

    @Override
    public List<SalesTaskResponseDto> findAllTasks() {
        return toResponseList(salesTaskRepository.findAll());
    }

    @Override
    public SalesTaskResponseDto findTasksById(Long id) {
        return toResponse(findSalesTaskEntityById(id));
    }

    @Override
    public SalesTaskResponseDto updateTask(SalesTaskRequestDto request, Long id) {

        SalesTask existingSalesTask = findSalesTaskEntityById(id);
        applyRequest(existingSalesTask, request);

        SalesTask updatedSalesTask = salesTaskRepository.save(existingSalesTask);

        return toResponse(updatedSalesTask);
    }

    @Override
    public SalesTaskResponseDto updateTaskStatus(Long id, TaskStatus taskStatus) {

        SalesTask existingSalesTask = findSalesTaskEntityById(id);
        existingSalesTask.setTaskStatus(taskStatus);

        return toResponse(salesTaskRepository.save(existingSalesTask));
    }

    @Override
    public void deleteTasksById(Long id) {
        SalesTask existingSalesTask = findSalesTaskEntityById(id);
        salesTaskRepository.delete(existingSalesTask);
    }

    @Override
    public List<SalesTaskResponseDto> findByLicenseId(Long licenseId) {
        return toResponseList(salesTaskRepository.findByLicenseId(licenseId));
    }

    @Override
    public List<SalesTaskResponseDto> findByRenewalId(Long renewalId) {
        return toResponseList(salesTaskRepository.findByRenewalId(renewalId));
    }

    @Override
    public List<SalesTaskResponseDto> findBySalespersonId(Long salespersonId) {
        return toResponseList(salesTaskRepository.findBySalespersonId(salespersonId));
    }

    @Override
    public List<SalesTaskResponseDto> findByLeadId(Long leadId) {
        return toResponseList(salesTaskRepository.findByLeadId(leadId));
    }

    @Override
    public List<SalesTaskResponseDto> findByCustomerId(Long customerId) {
        return toResponseList(salesTaskRepository.findByCustomerId(customerId));
    }

    @Override
    public List<SalesTaskResponseDto> findByTaskStatus(TaskStatus taskStatus) {
        return toResponseList(salesTaskRepository.findByTaskStatus(taskStatus));
    }

    @Override
    public List<SalesTaskResponseDto> findBySalespersonIdAndTaskStatus(Long salespersonId, TaskStatus taskStatus) {
        return toResponseList(salesTaskRepository.findBySalespersonIdAndTaskStatus(salespersonId, taskStatus));
    }

    @Override
    public List<SalesTaskResponseDto> findOverdueTasks() {
        return toResponseList(
                salesTaskRepository.findByDueDateBeforeAndTaskStatusIn(LocalDate.now(), OPEN_STATUSES));
    }

    /* ---------- Helpers ---------- */

    private SalesTask findSalesTaskEntityById(Long id) {
        return salesTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesTask not found with id: " + id));
    }

    private void applyRequest(SalesTask salesTask, SalesTaskRequestDto request) {
        salesTask.setTitle(request.title());
        salesTask.setDescription(request.description());
        salesTask.setTaskType(request.taskType() != null ? request.taskType() : "GENERAL");
        salesTask.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        salesTask.setTaskStatus(request.taskStatus() != null ? request.taskStatus() : TaskStatus.OPEN);
        salesTask.setDueDate(request.dueDate());

        salesTask.setCustomer(findCustomer(request.customerId()));
        salesTask.setLead(findLead(request.leadId()));
        salesTask.setLicense(findLicense(request.licenseId()));
        salesTask.setRenewal(findRenewal(request.renewalId()));
        salesTask.setSalesperson(findSalesperson(request.salespersonId()));
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

    private SalesPerson findSalesperson(Long salespersonId) {
        if (salespersonId == null) return null;
        return salesPersonRepository.findById(salespersonId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesPerson not found with id: " + salespersonId));
    }

    private List<SalesTaskResponseDto> toResponseList(List<SalesTask> salesTasks) {
        return salesTasks.stream()
                .map(this::toResponse)
                .toList();
    }

    private SalesTaskResponseDto toResponse(SalesTask salesTask) {

        boolean overdue = salesTask.getDueDate() != null
                && salesTask.getDueDate().isBefore(LocalDate.now())
                && OPEN_STATUSES.contains(salesTask.getTaskStatus());

        return new SalesTaskResponseDto(
                salesTask.getId(),
                salesTask.getCustomer() != null ? salesTask.getCustomer().getId() : null,
                salesTask.getLead() != null ? salesTask.getLead().getId() : null,
                salesTask.getLicense() != null ? salesTask.getLicense().getId() : null,
                salesTask.getRenewal() != null ? salesTask.getRenewal().getId() : null,
                salesTask.getSalesperson() != null ? salesTask.getSalesperson().getId() : null,
                salesTask.getTitle(),
                salesTask.getDescription(),
                salesTask.getTaskType(),
                salesTask.getPriority(),
                salesTask.getTaskStatus(),
                salesTask.getDueDate(),
                overdue,
                salesTask.getCreatedAt(),
                salesTask.getModifiedAt()
        );
    }
}
