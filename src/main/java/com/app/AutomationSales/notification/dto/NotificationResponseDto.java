package com.app.AutomationSales.notification.dto;

import com.app.AutomationSales.notification.model.NotificationStatus;

import java.sql.Timestamp;
import java.time.Instant;

public record NotificationResponseDto(
        Long notificationId,
        Long customerId,
        Long leadId,
        Long salesPersonId,
        Long licenseId,
        Long renewalId,
        String type,
        String recipientEmail,
        String subject,
        String message,
        NotificationStatus status,
        Timestamp scheduledAt,
        Timestamp sentAt,
        String failureReason,
        Instant createdAt,
        Instant modifiedAt
) {}
