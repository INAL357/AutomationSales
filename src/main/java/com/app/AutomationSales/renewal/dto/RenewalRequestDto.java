package com.app.AutomationSales.renewal.dto;

import com.app.AutomationSales.renewal.model.RenewalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * customerId is optional – if missing, the customer of the licence is used.
 * status defaults to PENDING.
 */
public record RenewalRequestDto(

        @NotNull(message = "licenseId is required")
        Long licenseId,

        Long customerId,

        RenewalStatus status,

        LocalDateTime nextRenewalDate,

        String customerIntent,

        @PositiveOrZero(message = "requestedLicenseCount cannot be negative")
        Integer requestedLicenseCount,

        @Positive(message = "requestedDurationMonths must be greater than 0")
        Integer requestedDurationMonths,

        @Size(max = 2000, message = "notes must be at most 2000 characters")
        String notes
) {}
