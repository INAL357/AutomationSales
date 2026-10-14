package com.app.AutomationSales.softwarevendor.repository;

import com.app.AutomationSales.softwarevendor.model.SoftwareVendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoftwareVendorRepository extends JpaRepository<SoftwareVendor, Long> {

    List<SoftwareVendor> findByVendorName(String vendorName);

    Optional<SoftwareVendor> findByVendorEmail(String vendorEmail);

    Optional<SoftwareVendor> findByVendorPhone(String vendorPhone);
}
