package com.app.AutomationSales.softwareproduct.repository;

import com.app.AutomationSales.softwareproduct.model.SoftwareProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoftwareProductRepository extends JpaRepository<SoftwareProduct, Long> {

    List<SoftwareProduct> findByProductName(String productName);

    List<SoftwareProduct> findByCategory(String category);

    List<SoftwareProduct> findByVendorId(Long vendorId);



}
