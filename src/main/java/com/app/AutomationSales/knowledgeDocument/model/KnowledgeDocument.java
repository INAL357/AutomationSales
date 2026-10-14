package com.app.AutomationSales.knowledgeDocument.model;

import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
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
@Table(name = "Knowledge_Document")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Document_Id")
    private Long id;

    private String title;
    private String description;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private SoftwareProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_license_id")
    private SoftwareLicense softwareLicense;

    private String type;
    private String documentUrl;
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;
}
