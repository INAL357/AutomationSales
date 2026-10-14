package com.app.AutomationSales.renewal.scheduler;

import com.app.AutomationSales.renewal.service.RenewalAutomationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the renewal check automatically.
 * The time comes from application.yaml -> app.renewal.scheduler-cron (default: every day at 08:00).
 * For testing you do not have to wait: call POST /api/renewals/automation/run in Postman.
 */
@Component
public class RenewalScheduler {

    private static final Logger log = LoggerFactory.getLogger(RenewalScheduler.class);

    private final RenewalAutomationService renewalAutomationService;

    public RenewalScheduler(RenewalAutomationService renewalAutomationService) {
        this.renewalAutomationService = renewalAutomationService;
    }

    @Scheduled(cron = "${app.renewal.scheduler-cron:0 0 8 * * *}")
    public void dailyLicenceCheck() {
        log.info("Scheduled renewal check starting...");
        try {
            renewalAutomationService.runDailyCheck();
        } catch (Exception ex) {
            // Never let one bad run kill the scheduler – log it and try again tomorrow
            log.error("Scheduled renewal check failed", ex);
        }
    }
}
