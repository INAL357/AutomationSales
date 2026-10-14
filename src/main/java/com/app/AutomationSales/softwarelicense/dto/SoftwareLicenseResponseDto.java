package com.app.AutomationSales.softwarelicense.dto;

import com.app.AutomationSales.softwarelicense.model.LicenseStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record SoftwareLicenseResponseDto(
        Long licenseId,
        Long productId,
        Long customerId,
        Long salespersonId,
        Integer licenseQuantity,
        Integer durationMonths,
        LocalDateTime purchaseDate,
        LocalDateTime expiryDate,
        Long daysRemaining,          // negative = already expired
        LicenseStatus status,
        Instant createdAt,
        Instant modifiedAt
) {}
