package com.app.AutomationSales.customer.controller;

import com.app.AutomationSales.customer.dto.CustomerRequestDTO;
import com.app.AutomationSales.customer.dto.CustomerResponseDTO;
import com.app.AutomationSales.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("@access.canAssignTo(#customerRequestDTO.salespersonId())")
    public ResponseEntity<CustomerResponseDTO> createCustomer(
            @Valid @RequestBody CustomerRequestDTO customerRequestDTO) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.createCustomer(customerRequestDTO));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerResponseDTO>> findAllCustomers() {
        return ResponseEntity.ok(customerService.findAllCustomers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canUseCustomer(#id)")
    public ResponseEntity<CustomerResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.canUseCustomer(#id) and @access.canAssignTo(#customerRequestDTO.salespersonId())")
    public ResponseEntity<CustomerResponseDTO> update(
            @Valid @RequestBody CustomerRequestDTO customerRequestDTO,
            @PathVariable Long id) {

        return ResponseEntity.ok(customerService.update(customerRequestDTO, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/salesperson/{salesPersonId}")
    @PreAuthorize("@access.isMe(#salesPersonId)")
    public ResponseEntity<List<CustomerResponseDTO>> findBySalespersonId(
            @PathVariable Long salesPersonId) {

        return ResponseEntity.ok(customerService.findBySalespersonId(salesPersonId));
    }
}
