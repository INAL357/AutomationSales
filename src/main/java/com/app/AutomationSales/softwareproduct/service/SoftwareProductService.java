package com.app.AutomationSales.softwareproduct.service;

import com.app.AutomationSales.softwareproduct.dto.SoftwareProductRequestDto;
import com.app.AutomationSales.softwareproduct.dto.SoftwareProductResponseDto;

import java.util.List;

public interface SoftwareProductService {

    SoftwareProductResponseDto createSoftwareProduct(SoftwareProductRequestDto request);

    List<SoftwareProductResponseDto> findAll();

    SoftwareProductResponseDto findProductId(Long id);

    SoftwareProductResponseDto updateProduct(SoftwareProductRequestDto request, Long id);

    void deleteProduct(Long id);

    List<SoftwareProductResponseDto> findByProductName(String productName);

    List<SoftwareProductResponseDto> findByCategory(String category);

    List<SoftwareProductResponseDto> findByVendorId(Long vendorId);
}
