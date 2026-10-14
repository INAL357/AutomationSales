package com.app.AutomationSales.renewal.model;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.salestask.model.SalesTask;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.notification.model.Notification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Renewal")
public class Renewal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Renewal_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    private SoftwareLicense license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // CHANGED: String -> enum PENDING / IN_PROGRESS / RENEWED / LOST
    @Enumerated(EnumType.STRING)
    private RenewalStatus status;

    private LocalDateTime nextRenewalDate;
    private String customerIntent;              // e.g. HIGH, MEDIUM, LOW, NOT_RENEWING
    private Integer requestedLicenseCount;      // CHANGED int -> Integer (empty until customer answers)
    private Integer requestedDurationMonths;    // CHANGED int -> Integer

    @Column(length = 2000)
    private String notes;

    // NEW: smallest reminder stage already handled by the scheduler (90, 60, 30, 14, 7).
    // Stops the scheduler from creating the same reminder twice.
    private Integer lastReminderDays;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @OneToMany(mappedBy = "renewal", fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "renewal", fetch = FetchType.LAZY)
    private List<SalesTask> salesTasks;
}
