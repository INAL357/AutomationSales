package com.app.AutomationSales.salescall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * A call must be linked to at least a customer or a lead (checked in the service).
 */
public record SalesCallRequestDto(

        Long customerId,
        Long salespersonId,
        Long leadId,
        Long renewalId,
        Long licenseId,

        @NotBlank(message = "callType is required")
        String callType,

        String callStatus,

        LocalDateTime startTime,

        LocalDateTime endTime,

        String transcript,

        @Size(max = 2000, message = "callSummary must be at most 2000 characters")
        String callSummary,

        String outcome,

        String nextAction
) {}
