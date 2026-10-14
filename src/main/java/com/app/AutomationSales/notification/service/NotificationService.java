package com.app.AutomationSales.notification.service;

import com.app.AutomationSales.notification.dto.NotificationRequestDto;
import com.app.AutomationSales.notification.dto.NotificationResponseDto;

import java.util.List;

public interface NotificationService {

    NotificationResponseDto createNotification(NotificationRequestDto request);

    List<NotificationResponseDto> findAll();

    NotificationResponseDto findViewById(Long id);

    NotificationResponseDto update(NotificationRequestDto request, Long id);

    void delete(Long id);

    List<NotificationResponseDto> findBySalesPersonId(Long salesPersonId);

    List<NotificationResponseDto> findByCustomerId(Long customerId);

    List<NotificationResponseDto> findByLicenseId(Long licenseId);

    List<NotificationResponseDto> findByRenewalId(Long renewalId);

    List<NotificationResponseDto> findByLeadId(Long leadId);
}
