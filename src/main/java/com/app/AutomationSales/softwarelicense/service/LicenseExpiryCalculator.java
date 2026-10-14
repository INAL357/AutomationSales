package com.app.AutomationSales.softwarelicense.service;

import com.app.AutomationSales.config.RenewalProperties;
import com.app.AutomationSales.softwarelicense.model.LicenseStatus;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * All "how many days are left / what status should this licence have" logic lives here,
 * so the licence service and the renewal scheduler always calculate it the same way.
 */
@Component
public class LicenseExpiryCalculator {

    private final RenewalProperties renewalProperties;

    public LicenseExpiryCalculator(RenewalProperties renewalProperties) {
        this.renewalProperties = renewalProperties;
    }

    /** Whole days from today until the expiry date. Negative = expired. Null if no expiry date. */
    public Long daysRemaining(LocalDateTime expiryDate) {
        if (expiryDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate.toLocalDate());
    }

    /**
     * Works out the status from the expiry date.
     * A licence that is already RENEWED keeps that status (it is finished).
     */
    public LicenseStatus calculateStatus(SoftwareLicense license) {

        if (license.getStatus() == LicenseStatus.RENEWED) {
            return LicenseStatus.RENEWED;
        }

        Long days = daysRemaining(license.getExpiryDate());

        if (days == null) {
            return LicenseStatus.ACTIVE;
        }
        if (days < 0) {
            return LicenseStatus.EXPIRED;
        }
        if (days <= renewalProperties.expiringSoonDays()) {
            return LicenseStatus.EXPIRING_SOON;
        }
        return LicenseStatus.ACTIVE;
    }

    /** True when the licence has not expired yet but expires within the given number of days. */
    public boolean isExpiringWithin(SoftwareLicense license, int days) {
        Long remaining = daysRemaining(license.getExpiryDate());
        return remaining != null && remaining >= 0 && remaining <= days;
    }
}
