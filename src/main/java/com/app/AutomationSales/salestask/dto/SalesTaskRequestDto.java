package com.app.AutomationSales.salestask.dto;

import com.app.AutomationSales.salestask.model.TaskPriority;
import com.app.AutomationSales.salestask.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * priority defaults to MEDIUM, taskStatus defaults to OPEN, taskType defaults to GENERAL.
 */
public record SalesTaskRequestDto(

        Long customerId,
        Long leadId,
        Long licenseId,
        Long renewalId,
        Long salespersonId,

        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        String taskType,

        TaskPriority priority,

        TaskStatus taskStatus,

        LocalDate dueDate
) {}
