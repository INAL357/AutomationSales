package com.app.AutomationSales.salescall.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record SalesCallResponseDto(
        Long salesCallId,
        Long customerId,
        Long salespersonId,
        Long leadId,
        Long renewalId,
        Long licenseId,
        String callType,
        String callStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long durationMinutes,
        String transcript,
        String callSummary,
        String outcome,
        String nextAction,
        Instant createdAt,
        Instant modifiedAt
) {}
