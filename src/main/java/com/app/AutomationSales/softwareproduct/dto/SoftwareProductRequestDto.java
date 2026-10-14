package com.app.AutomationSales.softwareproduct.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** active defaults to true when not sent. */
public record SoftwareProductRequestDto(

        @NotNull(message = "vendorId is required")
        Long vendorId,

        @NotBlank(message = "productName is required")
        @Size(max = 150, message = "productName must be at most 150 characters")
        String productName,

        @Size(max = 2000, message = "productDescription must be at most 2000 characters")
        String productDescription,

        @PositiveOrZero(message = "productPrice cannot be negative")
        Double productPrice,

        String category,

        String version,

        String platform,

        Boolean active
) {}
