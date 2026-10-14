package com.app.AutomationSales.lead.dto;

import com.app.AutomationSales.lead.model.LeadStatus;

import java.time.Instant;

public record LeadResponseDto(
        Long leadId,
        String leadName,
        Long productId,
        Long salespersonId,
        String companyName,
        String email,
        String phone,
        String source,
        String message,
        LeadStatus status,
        Integer leadScore,
        Integer requiredLicenses,
        Integer existingLicenses,
        Integer preferredDurationMonths,
        String purchaseTimeline,
        Instant createdAt,
        Instant modifiedAt
) {}
