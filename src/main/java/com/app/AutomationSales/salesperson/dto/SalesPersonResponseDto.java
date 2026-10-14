package com.app.AutomationSales.salesperson.dto;

import java.time.Instant;

public record SalesPersonResponseDto(
        Long salesPersonId,
        String salesPersonName,
        String salesPersonEmail,
        String salesPersonNumber,
        String salesPersonRole,
        String status,
        Instant createdAt,
        Instant modifiedAt
) {}
