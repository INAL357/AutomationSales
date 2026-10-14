package com.app.AutomationSales.softwarelicense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Either expiryDate OR durationMonths must be sent.
 * If expiryDate is missing it is calculated as purchaseDate + durationMonths.
 * Status is NOT sent by the client – it is calculated from the expiry date.
 */
public record SoftwareLicenseRequestDto(

        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "customerId is required")
        Long customerId,

        Long salespersonId,

        @NotNull(message = "licenseQuantity is required")
        @Positive(message = "licenseQuantity must be greater than 0")
        Integer licenseQuantity,

        @Positive(message = "durationMonths must be greater than 0")
        Integer durationMonths,

        @NotNull(message = "purchaseDate is required")
        LocalDateTime purchaseDate,

        LocalDateTime expiryDate
) {}
