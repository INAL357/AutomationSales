package com.app.AutomationSales.softwarevendor.service;

import com.app.AutomationSales.softwarevendor.dto.SoftwareVendorRequestDto;
import com.app.AutomationSales.softwarevendor.dto.SoftwareVendorResponseDto;

import java.util.List;

public interface SoftwareVendorService {

    SoftwareVendorResponseDto createSoftwareVendor(SoftwareVendorRequestDto request);

    List<SoftwareVendorResponseDto> findAll();

    SoftwareVendorResponseDto findVendorById(Long id);

    SoftwareVendorResponseDto updateVendor(SoftwareVendorRequestDto request, Long id);

    void deleteVendor(Long id);

    List<SoftwareVendorResponseDto> findByVendorName(String vendorName);

    SoftwareVendorResponseDto findByVendorEmail(String vendorEmail);

    SoftwareVendorResponseDto findByVendorPhone(String vendorPhone);
}
