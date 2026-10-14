package com.app.AutomationSales.salestask.controller;

import com.app.AutomationSales.salestask.dto.SalesTaskRequestDto;
import com.app.AutomationSales.salestask.dto.SalesTaskResponseDto;
import com.app.AutomationSales.salestask.model.TaskStatus;
import com.app.AutomationSales.salestask.service.SalesTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-tasks")
public class SalesTaskController {

    private final SalesTaskService salesTaskService;

    public SalesTaskController(SalesTaskService salesTaskService) {
        this.salesTaskService = salesTaskService;
    }

    @PostMapping
    @PreAuthorize("@access.canAssignTo(#request.salespersonId())")
    public ResponseEntity<SalesTaskResponseDto> createSalesTask(@Valid @RequestBody SalesTaskRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(salesTaskService.createSalesTask(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesTaskResponseDto>> findAllTasks() {
        return ResponseEntity.ok(salesTaskService.findAllTasks());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canUseTask(#id)")
    public ResponseEntity<SalesTaskResponseDto> getSalesTask(@PathVariable Long id) {
        return ResponseEntity.ok(salesTaskService.findTasksById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.canUseTask(#id) and @access.canAssignTo(#request.salespersonId())")
    public ResponseEntity<SalesTaskResponseDto> updateTask(
            @Valid @RequestBody SalesTaskRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(salesTaskService.updateTask(request, id));
    }

    // PATCH /api/sales-tasks/5/status?taskStatus=DONE   (quick way to tick a task off)
    @PatchMapping("/{id}/status")
    @PreAuthorize("@access.canUseTask(#id)")
    public ResponseEntity<SalesTaskResponseDto> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus taskStatus) {

        return ResponseEntity.ok(salesTaskService.updateTaskStatus(id, taskStatus));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTasksById(@PathVariable Long id) {
        salesTaskService.deleteTasksById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/license/{licenseId}")
    @PreAuthorize("@access.canUseLicense(#licenseId)")
    public ResponseEntity<List<SalesTaskResponseDto>> findByLicenseId(@PathVariable Long licenseId) {
        return ResponseEntity.ok(salesTaskService.findByLicenseId(licenseId));
    }

    @GetMapping("/renewal/{renewalId}")
    @PreAuthorize("@access.canUseRenewal(#renewalId)")
    public ResponseEntity<List<SalesTaskResponseDto>> findByRenewalId(@PathVariable Long renewalId) {
        return ResponseEntity.ok(salesTaskService.findByRenewalId(renewalId));
    }

    @GetMapping("/salesperson/{salespersonId}")
    @PreAuthorize("@access.isMe(#salespersonId)")
    public ResponseEntity<List<SalesTaskResponseDto>> findBySalespersonId(@PathVariable Long salespersonId) {
        return ResponseEntity.ok(salesTaskService.findBySalespersonId(salespersonId));
    }

    // GET /api/sales-tasks/salesperson/1/status?taskStatus=OPEN
    @GetMapping("/salesperson/{salespersonId}/status")
    @PreAuthorize("@access.isMe(#salespersonId)")
    public ResponseEntity<List<SalesTaskResponseDto>> findBySalespersonIdAndTaskStatus(
            @PathVariable Long salespersonId,
            @RequestParam TaskStatus taskStatus) {
        return ResponseEntity.ok(salesTaskService.findBySalespersonIdAndTaskStatus(salespersonId, taskStatus));
    }

    @GetMapping("/lead/{leadId}")
    @PreAuthorize("@access.canUseLead(#leadId)")
    public ResponseEntity<List<SalesTaskResponseDto>> findByLeadId(@PathVariable Long leadId) {
        return ResponseEntity.ok(salesTaskService.findByLeadId(leadId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@access.canUseCustomer(#customerId)")
    public ResponseEntity<List<SalesTaskResponseDto>> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(salesTaskService.findByCustomerId(customerId));
    }

    // GET /api/sales-tasks/status/OPEN
    @GetMapping("/status/{taskStatus}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesTaskResponseDto>> findByTaskStatus(@PathVariable TaskStatus taskStatus) {
        return ResponseEntity.ok(salesTaskService.findByTaskStatus(taskStatus));
    }

    // GET /api/sales-tasks/overdue
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesTaskResponseDto>> findOverdueTasks() {
        return ResponseEntity.ok(salesTaskService.findOverdueTasks());
    }
}
