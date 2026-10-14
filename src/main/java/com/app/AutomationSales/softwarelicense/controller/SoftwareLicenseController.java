package com.app.AutomationSales.softwarelicense.controller;

import com.app.AutomationSales.softwarelicense.dto.SoftwareLicenseRequestDto;
import com.app.AutomationSales.softwarelicense.dto.SoftwareLicenseResponseDto;
import com.app.AutomationSales.softwarelicense.model.LicenseStatus;
import com.app.AutomationSales.softwarelicense.service.SoftwareLicenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/licenses")
public class SoftwareLicenseController {

    private final SoftwareLicenseService softwareLicenseService;

    public SoftwareLicenseController(SoftwareLicenseService softwareLicenseService) {
        this.softwareLicenseService = softwareLicenseService;
    }

    @PostMapping
    @PreAuthorize("@access.canAssignTo(#request.salespersonId())")
    public ResponseEntity<SoftwareLicenseResponseDto> createSoftwareLicense(
            @Valid @RequestBody SoftwareLicenseRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(softwareLicenseService.createSoftwareLicense(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> findAllSoftwareLicense() {
        return ResponseEntity.ok(softwareLicenseService.findAllSoftwareLicense());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canUseLicense(#id)")
    public ResponseEntity<SoftwareLicenseResponseDto> findLicenseById(@PathVariable Long id) {
        return ResponseEntity.ok(softwareLicenseService.findLicenseById(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@access.canUseCustomer(#customerId)")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(softwareLicenseService.findByCustomerId(customerId));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(softwareLicenseService.findByProductId(productId));
    }

    @GetMapping("/salesperson/{salespersonId}")
    @PreAuthorize("@access.isMe(#salespersonId)")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> findBySalespersonId(@PathVariable Long salespersonId) {
        return ResponseEntity.ok(softwareLicenseService.findBySalespersonId(salespersonId));
    }

    // GET /api/licenses/status/EXPIRING_SOON
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> findByStatus(@PathVariable LicenseStatus status) {
        return ResponseEntity.ok(softwareLicenseService.findByStatus(status));
    }

    // GET /api/licenses/customer/1/status?status=ACTIVE
    @GetMapping("/customer/{customerId}/status")
    @PreAuthorize("@access.canUseCustomer(#customerId)")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> findByCustomerIdAndStatus(
            @PathVariable Long customerId, @RequestParam LicenseStatus status) {
        return ResponseEntity.ok(softwareLicenseService.findByCustomerIdAndStatus(customerId, status));
    }

    @GetMapping("/product/{productId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> findByProductIdAndStatus(
            @PathVariable Long productId, @RequestParam LicenseStatus status) {
        return ResponseEntity.ok(softwareLicenseService.findByProductIdAndStatus(productId, status));
    }

    @GetMapping("/salesperson/{salespersonId}/status")
    @PreAuthorize("@access.isMe(#salespersonId)")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> findBySalespersonIdAndStatus(
            @PathVariable Long salespersonId, @RequestParam LicenseStatus status) {
        return ResponseEntity.ok(softwareLicenseService.findBySalespersonIdAndStatus(salespersonId, status));
    }

    /* ---------- Expiry / renewal endpoints ---------- */

    // GET /api/licenses/expiring?days=30
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> getLicensesExpiringWithinDays(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(softwareLicenseService.getLicensesExpiringWithinDays(days));
    }

    // GET /api/licenses/expired
    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SoftwareLicenseResponseDto>> getExpiredLicenses() {
        return ResponseEntity.ok(softwareLicenseService.getExpiredLicenses());
    }

    // GET /api/licenses/{id}/expiring-soon?days=30
    @GetMapping("/{id}/expiring-soon")
    @PreAuthorize("@access.canUseLicense(#id)")
    public ResponseEntity<Boolean> isExpiringSoon(
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(softwareLicenseService.isExpiringSoon(id, days));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.canUseLicense(#id) and @access.canAssignTo(#request.salespersonId())")
    public ResponseEntity<SoftwareLicenseResponseDto> updateSoftwareLicense(
            @Valid @RequestBody SoftwareLicenseRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(softwareLicenseService.updateSoftwareLicense(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoftwareLicense(@PathVariable Long id) {
        softwareLicenseService.deleteSoftwareLicense(id);
        return ResponseEntity.noContent().build();
    }
}
