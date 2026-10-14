package com.app.AutomationSales.softwarevendor.dto;

import java.time.Instant;

public record SoftwareVendorResponseDto(
        Long vendorId,
        String vendorName,
        String vendorDescription,
        String vendorEmail,
        String vendorPhone,
        Instant createdAt,
        Instant modifiedAt
) {}
