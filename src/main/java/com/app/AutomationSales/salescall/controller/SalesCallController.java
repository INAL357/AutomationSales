package com.app.AutomationSales.salescall.controller;

import com.app.AutomationSales.salescall.dto.SalesCallRequestDto;
import com.app.AutomationSales.salescall.dto.SalesCallResponseDto;
import com.app.AutomationSales.salescall.service.SalesCallService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales-calls")
public class SalesCallController {

    private final SalesCallService salesCallService;

    public SalesCallController(SalesCallService salesCallService) {
        this.salesCallService = salesCallService;
    }

    @PostMapping
    @PreAuthorize("@access.canAssignTo(#request.salespersonId())")
    public ResponseEntity<SalesCallResponseDto> createSalesCall(@Valid @RequestBody SalesCallRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(salesCallService.createSalesCall(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesCallResponseDto>> findAll() {
        return ResponseEntity.ok(salesCallService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canUseCall(#id)")
    public ResponseEntity<SalesCallResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(salesCallService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.canUseCall(#id) and @access.canAssignTo(#request.salespersonId())")
    public ResponseEntity<SalesCallResponseDto> updateSalesCall(
            @Valid @RequestBody SalesCallRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(salesCallService.updateSalesCall(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalesCallById(@PathVariable Long id) {
        salesCallService.deleteSalesCallById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/salesperson/{salespersonId}")
    @PreAuthorize("@access.isMe(#salespersonId)")
    public ResponseEntity<List<SalesCallResponseDto>> findBySalespersonId(@PathVariable Long salespersonId) {
        return ResponseEntity.ok(salesCallService.findBySalespersonId(salespersonId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@access.canUseCustomer(#customerId)")
    public ResponseEntity<List<SalesCallResponseDto>> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(salesCallService.findByCustomerId(customerId));
    }

    @GetMapping("/lead/{leadId}")
    @PreAuthorize("@access.canUseLead(#leadId)")
    public ResponseEntity<List<SalesCallResponseDto>> findByLeadId(@PathVariable Long leadId) {
        return ResponseEntity.ok(salesCallService.findByLeadId(leadId));
    }

    @GetMapping("/renewal/{renewalId}")
    @PreAuthorize("@access.canUseRenewal(#renewalId)")
    public ResponseEntity<List<SalesCallResponseDto>> findByRenewalId(@PathVariable Long renewalId) {
        return ResponseEntity.ok(salesCallService.findByRenewalId(renewalId));
    }

    @GetMapping("/license/{licenseId}")
    @PreAuthorize("@access.canUseLicense(#licenseId)")
    public ResponseEntity<List<SalesCallResponseDto>> findByLicenseId(@PathVariable Long licenseId) {
        return ResponseEntity.ok(salesCallService.findByLicenseId(licenseId));
    }

    // GET /api/sales-calls/between?from=2026-10-01T00:00:00&to=2026-10-31T23:59:59
    @GetMapping("/between")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesCallResponseDto>> findByStartTimeBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(salesCallService.findByStartTimeBetween(from, to));
    }
}
