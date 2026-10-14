package com.app.AutomationSales.salestask.model;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A to-do item for a salesperson, e.g. "Call ABC Technologies – AutoCAD licence expires in 30 days".
 * (The call details – transcript, summary, outcome – now live in SalesCall.)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Sales_Task")
public class SalesTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Sales_Task_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    private SoftwareLicense license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renewal_id")
    private Renewal renewal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesperson_id")
    private SalesPerson salesperson;

    private String title;

    @Column(length = 2000)
    private String description;

    // e.g. RENEWAL, LEAD_FOLLOW_UP, GENERAL
    private String taskType;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    private LocalDate dueDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;
}
