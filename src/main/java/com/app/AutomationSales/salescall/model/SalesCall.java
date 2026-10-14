package com.app.AutomationSales.salescall.model;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * One phone call (human or AI) with a customer or lead.
 * Later the AI voice call will fill transcript / callSummary / outcome.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Sales_Call")
public class SalesCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Sales_Call_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesperson_id")
    private SalesPerson salesperson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renewal_id")
    private Renewal renewal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    private SoftwareLicense license;

    // MOVED here from SalesTask
    private String callType;        // e.g. RENEWAL, LEAD_QUALIFICATION, FOLLOW_UP
    private String callStatus;      // e.g. SCHEDULED, COMPLETED, NO_ANSWER, FAILED
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Column(length = 2000)
    private String callSummary;

    private String outcome;         // e.g. INTERESTED, NOT_INTERESTED, CALL_BACK, ESCALATE
    private String nextAction;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;
}
