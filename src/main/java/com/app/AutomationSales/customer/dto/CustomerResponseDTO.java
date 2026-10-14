package com.app.AutomationSales.customer.dto;

import java.time.Instant;

public record CustomerResponseDTO(
        Long customerId,
        String customerName,
        String customerEmail,
        String customerPhone,
        String companyName,
        Long salespersonId,
        String status,
        Instant createdAt,
        Instant modifiedAt
) {}
