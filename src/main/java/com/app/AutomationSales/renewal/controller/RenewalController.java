package com.app.AutomationSales.renewal.controller;

import com.app.AutomationSales.renewal.dto.RenewalAutomationResultDto;
import com.app.AutomationSales.renewal.dto.RenewalRequestDto;
import com.app.AutomationSales.renewal.dto.RenewalResponseDto;
import com.app.AutomationSales.renewal.model.RenewalStatus;
import com.app.AutomationSales.renewal.service.RenewalAutomationService;
import com.app.AutomationSales.renewal.service.RenewalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renewals")
public class RenewalController {

    private final RenewalService renewalService;
    private final RenewalAutomationService renewalAutomationService;

    public RenewalController(RenewalService renewalService,
                             RenewalAutomationService renewalAutomationService) {
        this.renewalService = renewalService;
        this.renewalAutomationService = renewalAutomationService;
    }

    @PostMapping
    @PreAuthorize("@access.canUseLicense(#request.licenseId())")
    public ResponseEntity<RenewalResponseDto> createRenewal(@Valid @RequestBody RenewalRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(renewalService.createRenewal(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RenewalResponseDto>> renewalList() {
        return ResponseEntity.ok(renewalService.renewalList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canUseRenewal(#id)")
    public ResponseEntity<RenewalResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(renewalService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.canUseRenewal(#id) and @access.canUseLicense(#request.licenseId())")
    public ResponseEntity<RenewalResponseDto> updateRenewal(
            @Valid @RequestBody RenewalRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(renewalService.updateRenewal(request, id));
    }

    // PATCH /api/renewals/3/status?status=RENEWED
    @PatchMapping("/{id}/status")
    @PreAuthorize("@access.canUseRenewal(#id)")
    public ResponseEntity<RenewalResponseDto> updateRenewalStatus(
            @PathVariable Long id,
            @RequestParam RenewalStatus status) {

        return ResponseEntity.ok(renewalService.updateRenewalStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        renewalService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/license/{licenseId}")
    @PreAuthorize("@access.canUseLicense(#licenseId)")
    public ResponseEntity<List<RenewalResponseDto>> findByLicenseId(@PathVariable Long licenseId) {
        return ResponseEntity.ok(renewalService.findByLicenseId(licenseId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@access.canUseCustomer(#customerId)")
    public ResponseEntity<List<RenewalResponseDto>> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(renewalService.findByCustomerId(customerId));
    }

    // GET /api/renewals/salesperson/3  -> renewals of this salesperson's licences
    @GetMapping("/salesperson/{salesPersonId}")
    @PreAuthorize("@access.isMe(#salesPersonId)")
    public ResponseEntity<List<RenewalResponseDto>> findBySalespersonId(@PathVariable Long salesPersonId) {
        return ResponseEntity.ok(renewalService.findBySalespersonId(salesPersonId));
    }

    // GET /api/renewals/status/PENDING
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RenewalResponseDto>> findByStatus(@PathVariable RenewalStatus status) {
        return ResponseEntity.ok(renewalService.findByStatus(status));
    }

    // POST /api/renewals/automation/run  -> runs the daily check right now (for testing)
    @PostMapping("/automation/run")
    public ResponseEntity<RenewalAutomationResultDto> runAutomationNow() {
        return ResponseEntity.ok(renewalAutomationService.runDailyCheck());
    }
}
