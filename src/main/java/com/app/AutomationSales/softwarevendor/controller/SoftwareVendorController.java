package com.app.AutomationSales.softwarevendor.controller;

import com.app.AutomationSales.softwarevendor.dto.SoftwareVendorRequestDto;
import com.app.AutomationSales.softwarevendor.dto.SoftwareVendorResponseDto;
import com.app.AutomationSales.softwarevendor.service.SoftwareVendorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
public class SoftwareVendorController {

    private final SoftwareVendorService softwareVendorService;

    public SoftwareVendorController(SoftwareVendorService softwareVendorService) {
        this.softwareVendorService = softwareVendorService;
    }

    @PostMapping
    public ResponseEntity<SoftwareVendorResponseDto> create(@Valid @RequestBody SoftwareVendorRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(softwareVendorService.createSoftwareVendor(request));
    }

    @GetMapping
    public ResponseEntity<List<SoftwareVendorResponseDto>> findAll() {
        return ResponseEntity.ok(softwareVendorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoftwareVendorResponseDto> findVendorById(@PathVariable Long id) {
        return ResponseEntity.ok(softwareVendorService.findVendorById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<SoftwareVendorResponseDto>> findByVendorName(@PathVariable String name) {
        return ResponseEntity.ok(softwareVendorService.findByVendorName(name));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<SoftwareVendorResponseDto> findByVendorEmail(@PathVariable String email) {
        return ResponseEntity.ok(softwareVendorService.findByVendorEmail(email));
    }

    @GetMapping("/phone/{vendorPhone}")
    public ResponseEntity<SoftwareVendorResponseDto> findByVendorPhone(@PathVariable String vendorPhone) {
        return ResponseEntity.ok(softwareVendorService.findByVendorPhone(vendorPhone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SoftwareVendorResponseDto> updateVendor(
            @Valid @RequestBody SoftwareVendorRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(softwareVendorService.updateVendor(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        softwareVendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }
}
