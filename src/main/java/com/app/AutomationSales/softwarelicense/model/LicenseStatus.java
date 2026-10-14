package com.app.AutomationSales.softwarelicense.model;

/**
 * Lifecycle status of a software licence.
 * ACTIVE / EXPIRING_SOON / EXPIRED are calculated automatically from the expiry date.
 * RENEWED is set when the licence's renewal is completed (it then stops getting reminders).
 */
public enum LicenseStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED,
    RENEWED
}
