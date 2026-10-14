package com.app.AutomationSales.softwarelicense.service;

import com.app.AutomationSales.softwarelicense.dto.SoftwareLicenseRequestDto;
import com.app.AutomationSales.softwarelicense.dto.SoftwareLicenseResponseDto;
import com.app.AutomationSales.softwarelicense.model.LicenseStatus;

import java.util.List;

public interface SoftwareLicenseService {

    SoftwareLicenseResponseDto createSoftwareLicense(SoftwareLicenseRequestDto request);

    List<SoftwareLicenseResponseDto> findAllSoftwareLicense();

    SoftwareLicenseResponseDto findLicenseById(Long id);

    SoftwareLicenseResponseDto updateSoftwareLicense(SoftwareLicenseRequestDto request, Long id);

    void deleteSoftwareLicense(Long id);

    List<SoftwareLicenseResponseDto> findByCustomerId(Long customerId);

    List<SoftwareLicenseResponseDto> findByProductId(Long productId);

    List<SoftwareLicenseResponseDto> findBySalespersonId(Long salespersonId);

    List<SoftwareLicenseResponseDto> findByStatus(LicenseStatus status);

    List<SoftwareLicenseResponseDto> findByCustomerIdAndStatus(Long customerId, LicenseStatus status);

    List<SoftwareLicenseResponseDto> findByProductIdAndStatus(Long productId, LicenseStatus status);

    List<SoftwareLicenseResponseDto> findBySalespersonIdAndStatus(Long salespersonId, LicenseStatus status);

    /* Logic for license expiry date and renewals */

    List<SoftwareLicenseResponseDto> getLicensesExpiringWithinDays(int days);

    List<SoftwareLicenseResponseDto> getExpiredLicenses();

    boolean isExpiringSoon(Long licenseId, int days);
}
