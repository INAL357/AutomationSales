package com.app.AutomationSales.customer.model;

import com.app.AutomationSales.salescall.model.SalesCall;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salestask.model.SalesTask;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Long id;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String companyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesperson_id")
    private SalesPerson salesperson;

    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<SoftwareLicense> licenses;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<SalesCall> salesCalls;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<SalesTask> salesTasks;
}
