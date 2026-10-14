package com.app.AutomationSales.notification.controller;

import com.app.AutomationSales.notification.dto.NotificationRequestDto;
import com.app.AutomationSales.notification.dto.NotificationResponseDto;
import com.app.AutomationSales.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @PreAuthorize("@access.canAssignTo(#request.salesPersonId())")
    public ResponseEntity<NotificationResponseDto> createNotification(
            @Valid @RequestBody NotificationRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationService.createNotification(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponseDto>> findAll() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canUseNotification(#id)")
    public ResponseEntity<NotificationResponseDto> findViewById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.findViewById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.canUseNotification(#id) and @access.canAssignTo(#request.salesPersonId())")
    public ResponseEntity<NotificationResponseDto> update(
            @Valid @RequestBody NotificationRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(notificationService.update(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/salesperson/{salesPersonId}")
    @PreAuthorize("@access.isMe(#salesPersonId)")
    public ResponseEntity<List<NotificationResponseDto>> findBySalesPersonId(@PathVariable Long salesPersonId) {
        return ResponseEntity.ok(notificationService.findBySalesPersonId(salesPersonId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@access.canUseCustomer(#customerId)")
    public ResponseEntity<List<NotificationResponseDto>> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.findByCustomerId(customerId));
    }

    @GetMapping("/license/{licenseId}")
    @PreAuthorize("@access.canUseLicense(#licenseId)")
    public ResponseEntity<List<NotificationResponseDto>> findByLicenseId(@PathVariable Long licenseId) {
        return ResponseEntity.ok(notificationService.findByLicenseId(licenseId));
    }

    @GetMapping("/renewal/{renewalId}")
    @PreAuthorize("@access.canUseRenewal(#renewalId)")
    public ResponseEntity<List<NotificationResponseDto>> findByRenewalId(@PathVariable Long renewalId) {
        return ResponseEntity.ok(notificationService.findByRenewalId(renewalId));
    }

    @GetMapping("/lead/{leadId}")
    @PreAuthorize("@access.canUseLead(#leadId)")
    public ResponseEntity<List<NotificationResponseDto>> findByLeadId(@PathVariable Long leadId) {
        return ResponseEntity.ok(notificationService.findByLeadId(leadId));
    }
}
