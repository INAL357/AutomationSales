package com.app.AutomationSales.notification.service;

import com.app.AutomationSales.emaildetails.EmailDetails;
import com.app.AutomationSales.emaildetails.EmailService;
import com.app.AutomationSales.notification.model.Notification;
import com.app.AutomationSales.notification.model.NotificationStatus;
import com.app.AutomationSales.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class NotificationDeliveryService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationDeliveryService(
            NotificationRepository notificationRepository,
            EmailService emailService
    ) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public void sendPendingNotifications() {

        Timestamp now = Timestamp.from(Instant.now());

        List<Notification> notifications =
                notificationRepository.findReadyToSend(
                        NotificationStatus.PENDING,
                        now
                );

        for (Notification notification : notifications) {

            try {

                EmailDetails emailDetails =
                        new EmailDetails();

                emailDetails.setRecipient(
                        notification.getRecipientEmail()
                );

                emailDetails.setSubject(
                        notification.getSubject()
                );

                emailDetails.setMsgBody(
                        notification.getMessage()
                );

                emailDetails.setAttachment(null);

                emailService.sendSimpleMail(emailDetails);

                notification.setStatus(
                        NotificationStatus.SENT
                );

                notification.setSentAt(now);

                notification.setFailureReason(null);

            } catch (Exception e) {

                notification.setStatus(
                        NotificationStatus.FAILED
                );

                notification.setFailureReason(
                        e.getMessage()
                );
            }

            notificationRepository.save(notification);
        }
    }
}