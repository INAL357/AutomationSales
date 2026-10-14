package com.app.AutomationSales.lead.model;

import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.softwareproduct.model.SoftwareProduct;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Lead")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Lead_Id")
    private Long id;

    private String leadName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private SoftwareProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesperson_id")
    private SalesPerson salesperson;

    private String companyName;
    private String email;
    private String phone;
    private String source;

    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status;

    private Integer leadScore;

    // Qualification answers. A salesperson can fill these now; the AI call can fill them later.
    private Integer requiredLicenses;
    private Integer existingLicenses;
    private Integer preferredDurationMonths;
    private String purchaseTimeline;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;
}
