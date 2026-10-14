package com.app.AutomationSales.renewal.model;

/**
 * PENDING      – created automatically by the scheduler, nobody has acted yet
 * IN_PROGRESS  – salesperson / AI is talking to the customer
 * RENEWED      – customer renewed (licence is marked RENEWED, reminders stop)
 * LOST         – customer will not renew (reminders stop)
 */
public enum RenewalStatus {
    PENDING,
    IN_PROGRESS,
    RENEWED,
    LOST
}
