package com.app.AutomationSales.notification.dto;

import com.app.AutomationSales.notification.model.NotificationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;

/** status defaults to PENDING when not sent. */
public record NotificationRequestDto(
        Long customerId,
        Long leadId,
        Long salesPersonId,
        Long licenseId,
        Long renewalId,

        @NotBlank(message = "type is required")
        String type,

        @NotBlank(message = "recipientEmail is required")
        @Email(message = "recipientEmail must be a valid email address")
        String recipientEmail,

        @NotBlank(message = "subject is required")
        @Size(max = 255, message = "subject must be at most 255 characters")
        String subject,

        @NotBlank(message = "message is required")
        @Size(max = 2000, message = "message must be at most 2000 characters")
        String message,

        NotificationStatus status,
        Timestamp scheduledAt,
        Timestamp sentAt
) {}
