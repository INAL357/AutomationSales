package com.app.AutomationSales.lead.dto;

import com.app.AutomationSales.lead.model.LeadStatus;
import jakarta.validation.constraints.NotNull;

public record LeadStatusUpdateRequestDto(
        @NotNull(message = "status is required")
        LeadStatus status
) {}
