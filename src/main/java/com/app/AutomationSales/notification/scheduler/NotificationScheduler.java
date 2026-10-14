package com.app.AutomationSales.notification.scheduler;

import com.app.AutomationSales.notification.service.NotificationDeliveryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private final NotificationDeliveryService notificationDeliveryService;

    public NotificationScheduler(NotificationDeliveryService notificationDeliveryService) {
        this.notificationDeliveryService = notificationDeliveryService;
    }

    @Scheduled(fixedDelayString = "${app.notification.email-delay-ms:60000}")
    public void sendPendingEmails() {
        notificationDeliveryService.sendPendingNotifications();
    }
}
