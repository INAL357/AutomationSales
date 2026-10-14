package com.app.AutomationSales.softwarelicense.model;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.knowledgeDocument.model.KnowledgeDocument;
import com.app.AutomationSales.softwareproduct.model.SoftwareProduct;
import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.salesperson.model.SalesPerson;
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
@Table(name = "Software_License")
public class SoftwareLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Software_License_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private SoftwareProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesperson_id")
    private SalesPerson salesperson;

    // NEW: number of licences/seats purchased (e.g. 20)
    private Integer licenseQuantity;

    // NEW: contract length in months (12 = 1 year, 36 = 3 years, 60 = 5 years ...)
    private Integer durationMonths;

    private LocalDateTime purchaseDate;
    private LocalDateTime expiryDate;

    // CHANGED: was "boolean status" – now ACTIVE / EXPIRING_SOON / EXPIRED / RENEWED
    @Enumerated(EnumType.STRING)
    private LicenseStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @OneToMany(mappedBy = "softwareLicense", fetch = FetchType.LAZY)
    private List<KnowledgeDocument> knowledgeDocuments;

    @OneToMany(mappedBy = "license", fetch = FetchType.LAZY)
    private List<Renewal> renewals;
}
