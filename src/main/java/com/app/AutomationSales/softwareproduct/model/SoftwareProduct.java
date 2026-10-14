package com.app.AutomationSales.softwareproduct.model;

import com.app.AutomationSales.knowledgeDocument.model.KnowledgeDocument;
import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.softwarevendor.model.SoftwareVendor;
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
@Table(name = "Software_Product")
public class SoftwareProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Software_Product_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private SoftwareVendor vendor;

    private String productName;
    private String productDescription;
    private Double productPrice;
    private String category;
    private String version;
    private String platform;
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<SoftwareLicense> licenses;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<Lead> leads;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<KnowledgeDocument> knowledgeDocuments;
}
