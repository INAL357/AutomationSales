package com.app.AutomationSales.softwareproduct.dto;

import java.time.Instant;

public record SoftwareProductResponseDto(
        Long productId,
        Long vendorId,
        String productName,
        String productDescription,
        Double productPrice,
        String category,
        String version,
        String platform,
        boolean active,
        Instant createdAt,
        Instant modifiedAt
) {}
