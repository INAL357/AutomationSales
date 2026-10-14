package com.app.AutomationSales.renewal.service;

import com.app.AutomationSales.renewal.dto.RenewalRequestDto;
import com.app.AutomationSales.renewal.dto.RenewalResponseDto;
import com.app.AutomationSales.renewal.model.RenewalStatus;

import java.util.List;

public interface RenewalService {

    RenewalResponseDto createRenewal(RenewalRequestDto request);

    List<RenewalResponseDto> renewalList();

    RenewalResponseDto findById(Long id);

    RenewalResponseDto updateRenewal(RenewalRequestDto request, Long id);

    RenewalResponseDto updateRenewalStatus(Long id, RenewalStatus status);

    void deleteById(Long id);

    List<RenewalResponseDto> findByLicenseId(Long licenseId);

    List<RenewalResponseDto> findByStatus(RenewalStatus status);

    List<RenewalResponseDto> findByCustomerId(Long customerId);

    List<RenewalResponseDto> findBySalespersonId(Long salesPersonId);
}
