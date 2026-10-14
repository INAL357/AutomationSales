package com.app.AutomationSales.renewal.dto;

import com.app.AutomationSales.renewal.model.RenewalStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record RenewalResponseDto(
        Long renewalId,
        Long licenseId,
        Long customerId,
        RenewalStatus status,
        LocalDateTime nextRenewalDate,
        String customerIntent,
        Integer requestedLicenseCount,
        Integer requestedDurationMonths,
        String notes,
        Integer lastReminderDays,
        Instant createdAt,
        Instant modifiedAt
) {}
