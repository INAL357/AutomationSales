package com.app.AutomationSales.notification.repository;

import com.app.AutomationSales.notification.model.Notification;
import com.app.AutomationSales.notification.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findBySalesPersonId(Long salesPersonId);
    List<Notification> findByCustomerId(Long customerId);
    List<Notification> findByLicenseId(Long licenseId);
    List<Notification> findByRenewalId(Long renewalId);
    List<Notification> findByLeadId(Long leadId);
    List<Notification> findByStatus(NotificationStatus status);

    @Query("""
            SELECT n
            FROM Notification n
            WHERE n.status = :status
              AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)
            ORDER BY n.createdAt ASC
            """)
    List<Notification> findReadyToSend(
            @Param("status") NotificationStatus status,
            @Param("now") Timestamp now
    );
}
