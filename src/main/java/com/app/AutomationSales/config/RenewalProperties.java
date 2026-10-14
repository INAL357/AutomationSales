package com.app.AutomationSales.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Renewal automation settings, read from application.yaml (app.renewal.*).
 * Change the reminder days there – no code change needed.
 *
 *   reminderDays      – days before expiry when a reminder/task is created, e.g. [90, 60, 30, 14, 7]
 *   expiringSoonDays  – a licence with this many days (or fewer) left is marked EXPIRING_SOON
 *   taskDueInDays     – how many days the salesperson gets to act on an automatic task
 */
@ConfigurationProperties(prefix = "app.renewal")
public record RenewalProperties(
        List<Integer> reminderDays,
        Integer expiringSoonDays,
        Integer taskDueInDays
) {
    public RenewalProperties {
        if (reminderDays == null || reminderDays.isEmpty()) {
            reminderDays = List.of(90, 60, 30, 14, 7);
        }
        if (expiringSoonDays == null) {
            expiringSoonDays = 90;
        }
        if (taskDueInDays == null) {
            taskDueInDays = 3;
        }
    }
}
