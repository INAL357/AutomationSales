package com.app.AutomationSales.lead.controller;

import com.app.AutomationSales.lead.dto.LeadRequestDto;
import com.app.AutomationSales.lead.dto.LeadResponseDto;
import com.app.AutomationSales.lead.dto.LeadStatusUpdateRequestDto;
import com.app.AutomationSales.lead.dto.PublicLeadRequestDto;
import com.app.AutomationSales.lead.model.LeadStatus;
import com.app.AutomationSales.lead.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    @PreAuthorize("@access.canAssignTo(#request.salespersonId())")
    public ResponseEntity<LeadResponseDto> createLead(@Valid @RequestBody LeadRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leadService.createLead(request));
    }

    // Public website-enquiry endpoint. The caller cannot choose status, score or salesperson.
    @PostMapping("/public")
    public ResponseEntity<LeadResponseDto> createWebsiteLead(
            @Valid @RequestBody PublicLeadRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leadService.createWebsiteLead(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeadResponseDto>> findAllLeads() {
        return ResponseEntity.ok(leadService.findAllLeads());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canUseLead(#id)")
    public ResponseEntity<LeadResponseDto> findLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(leadService.findLeadById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.canUseLead(#id) and @access.canAssignTo(#request.salespersonId())")
    public ResponseEntity<LeadResponseDto> updateLead(
            @Valid @RequestBody LeadRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(leadService.updateLead(request, id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@access.canUseLead(#id)")
    public ResponseEntity<LeadResponseDto> updateLeadStatus(
            @PathVariable Long id,
            @Valid @RequestBody LeadStatusUpdateRequestDto request) {

        return ResponseEntity.ok(leadService.updateLeadStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeadById(@PathVariable Long id) {
        leadService.deleteLeadById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/salesperson/{salesPersonId}")
    @PreAuthorize("@access.isMe(#salesPersonId)")
    public ResponseEntity<List<LeadResponseDto>> findBySalespersonId(@PathVariable Long salesPersonId) {
        return ResponseEntity.ok(leadService.findBySalespersonId(salesPersonId));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeadResponseDto>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(leadService.findByProductId(productId));
    }

    @GetMapping("/company/{companyName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeadResponseDto>> findByCompanyName(@PathVariable String companyName) {
        return ResponseEntity.ok(leadService.findByCompanyName(companyName));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeadResponseDto>> findByStatus(@PathVariable LeadStatus status) {
        return ResponseEntity.ok(leadService.findByStatus(status));
    }
}
