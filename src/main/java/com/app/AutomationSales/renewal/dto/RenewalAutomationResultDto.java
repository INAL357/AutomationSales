package com.app.AutomationSales.renewal.dto;

import java.time.LocalDateTime;

/**
 * Summary of one run of the daily renewal check (returned by POST /api/renewals/automation/run).
 */
public record RenewalAutomationResultDto(
        LocalDateTime ranAt,
        int licensesChecked,
        int statusesUpdated,
        int renewalsCreated,
        int tasksCreated,
        int notificationsCreated,
        int skippedAlreadyReminded
) {}
