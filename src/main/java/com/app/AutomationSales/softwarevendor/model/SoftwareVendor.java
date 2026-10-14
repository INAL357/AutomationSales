package com.app.AutomationSales.softwarevendor.model;

import com.app.AutomationSales.softwareproduct.model.SoftwareProduct;
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
@Table(name = "Software_Vendor")
public class SoftwareVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Software_vendor_Id")
    private Long id;

    private String vendorName;
    private String vendorDescription;

    @Column(unique = true)
    private String vendorEmail;

    @Column(unique = true)
    private String vendorPhone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @OneToMany(mappedBy = "vendor", fetch = FetchType.LAZY)
    private List<SoftwareProduct> products;
}
