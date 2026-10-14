package com.app.AutomationSales.renewal.service;

import com.app.AutomationSales.renewal.dto.RenewalAutomationResultDto;

public interface RenewalAutomationService {

    /**
     * The daily licence check:
     *  1. recalculate every licence status (ACTIVE / EXPIRING_SOON / EXPIRED)
     *  2. for licences that reached a reminder stage (90/60/30/14/7 days):
     *     create/find the Renewal, create a SalesTask, create internal + customer Notifications
     */
    RenewalAutomationResultDto runDailyCheck();
}
