package com.app.AutomationSales.salescall.service;

import com.app.AutomationSales.salescall.dto.SalesCallRequestDto;
import com.app.AutomationSales.salescall.dto.SalesCallResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesCallService {

    SalesCallResponseDto createSalesCall(SalesCallRequestDto request);

    List<SalesCallResponseDto> findAll();

    SalesCallResponseDto findById(Long id);

    SalesCallResponseDto updateSalesCall(SalesCallRequestDto request, Long id);

    void deleteSalesCallById(Long id);

    List<SalesCallResponseDto> findBySalespersonId(Long salespersonId);

    List<SalesCallResponseDto> findByCustomerId(Long customerId);

    List<SalesCallResponseDto> findByLeadId(Long leadId);

    List<SalesCallResponseDto> findByRenewalId(Long renewalId);

    List<SalesCallResponseDto> findByLicenseId(Long licenseId);

    List<SalesCallResponseDto> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
}
