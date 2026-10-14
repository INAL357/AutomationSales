package com.app.AutomationSales.salesperson.controller;

import com.app.AutomationSales.salesperson.dto.SalesPersonRequestDto;
import com.app.AutomationSales.salesperson.dto.SalesPersonResponseDto;
import com.app.AutomationSales.salesperson.service.SalesPersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salespersons")
public class SalesPersonController {

    private final SalesPersonService salesPersonService;

    public SalesPersonController(SalesPersonService salesPersonService) {
        this.salesPersonService = salesPersonService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesPersonResponseDto> createSalesPerson(
            @Valid @RequestBody SalesPersonRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(salesPersonService.createSalesPerson(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesPersonResponseDto>> findAllSalesPerson() {
        return ResponseEntity.ok(salesPersonService.findAllSalesPerson());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesPersonResponseDto> findSalesPersonById(@PathVariable Long id) {
        return ResponseEntity.ok(salesPersonService.findSalesPersonById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesPersonResponseDto> updateSalesPerson(
            @Valid @RequestBody SalesPersonRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(salesPersonService.updateSalesPerson(request, id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSalesPersonById(@PathVariable Long id) {
        salesPersonService.deleteSalesPersonById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesPersonResponseDto> findBySalesPersonEmail(@PathVariable String email) {
        return ResponseEntity.ok(salesPersonService.findBySalesPersonEmail(email));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesPersonResponseDto>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(salesPersonService.findByStatus(status));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesPersonResponseDto>> findBySalesPersonRole(@PathVariable String role) {
        return ResponseEntity.ok(salesPersonService.findBySalesPersonRole(role));
    }
}
