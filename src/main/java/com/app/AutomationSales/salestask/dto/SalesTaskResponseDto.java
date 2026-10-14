package com.app.AutomationSales.salestask.dto;

import com.app.AutomationSales.salestask.model.TaskPriority;
import com.app.AutomationSales.salestask.model.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record SalesTaskResponseDto(
        Long salesTaskId,
        Long customerId,
        Long leadId,
        Long licenseId,
        Long renewalId,
        Long salespersonId,
        String title,
        String description,
        String taskType,
        TaskPriority priority,
        TaskStatus taskStatus,
        LocalDate dueDate,
        boolean overdue,
        Instant createdAt,
        Instant modifiedAt
) {}
