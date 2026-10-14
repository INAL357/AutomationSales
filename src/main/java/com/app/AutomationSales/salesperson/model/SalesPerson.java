package com.app.AutomationSales.salesperson.model;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.salestask.model.SalesTask;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.salescall.model.SalesCall;
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
@Table(name = "Sales_Person")
public class SalesPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Sales_Person_Id")
    private Long id;

    private String salesPersonName;
    private String salesPersonEmail;
    private String salesPersonNumber;
    private String salesPersonRole;
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @OneToMany(mappedBy = "salesperson", fetch = FetchType.LAZY)
    private List<Customer> customers;

    @OneToMany(mappedBy = "salesperson", fetch = FetchType.LAZY)
    private List<Lead> leads;

    @OneToMany(mappedBy = "salesperson", fetch = FetchType.LAZY)
    private List<SoftwareLicense> licenses;

    @OneToMany(mappedBy = "salesperson", fetch = FetchType.LAZY)
    private List<SalesCall> salesCalls;

    @OneToMany(mappedBy = "salesperson", fetch = FetchType.LAZY)
    private List<SalesTask> salesTasks;
}
