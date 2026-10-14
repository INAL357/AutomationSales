package com.app.AutomationSales.softwareproduct.controller;

import com.app.AutomationSales.softwareproduct.dto.SoftwareProductRequestDto;
import com.app.AutomationSales.softwareproduct.dto.SoftwareProductResponseDto;
import com.app.AutomationSales.softwareproduct.service.SoftwareProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class SoftwareProductController {

    private final SoftwareProductService softwareProductService;

    public SoftwareProductController(SoftwareProductService softwareProductService) {
        this.softwareProductService = softwareProductService;
    }

    @PostMapping
    public ResponseEntity<SoftwareProductResponseDto> createSoftwareProduct(
            @Valid @RequestBody SoftwareProductRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(softwareProductService.createSoftwareProduct(request));
    }

    @GetMapping
    public ResponseEntity<List<SoftwareProductResponseDto>> findAll() {
        return ResponseEntity.ok(softwareProductService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoftwareProductResponseDto> findProductId(@PathVariable Long id) {
        return ResponseEntity.ok(softwareProductService.findProductId(id));
    }

    @GetMapping("/name/{productName}")
    public ResponseEntity<List<SoftwareProductResponseDto>> findByProductName(@PathVariable String productName) {
        return ResponseEntity.ok(softwareProductService.findByProductName(productName));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<SoftwareProductResponseDto>> findByCategory(@PathVariable String category) {
        return ResponseEntity.ok(softwareProductService.findByCategory(category));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<SoftwareProductResponseDto>> findByVendorId(@PathVariable Long vendorId) {
        return ResponseEntity.ok(softwareProductService.findByVendorId(vendorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SoftwareProductResponseDto> updateProduct(
            @Valid @RequestBody SoftwareProductRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(softwareProductService.updateProduct(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        softwareProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
