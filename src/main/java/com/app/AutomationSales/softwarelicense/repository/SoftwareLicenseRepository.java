package com.app.AutomationSales.softwarelicense.repository;

import com.app.AutomationSales.softwarelicense.model.LicenseStatus;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SoftwareLicenseRepository extends JpaRepository<SoftwareLicense, Long> {

    List<SoftwareLicense> findByCustomerId(Long customerId);

    List<SoftwareLicense> findByProductId(Long productId);

    List<SoftwareLicense> findBySalespersonId(Long salespersonId);

    List<SoftwareLicense> findByStatus(LicenseStatus status);

    List<SoftwareLicense> findByCustomerIdAndStatus(Long customerId, LicenseStatus status);

    List<SoftwareLicense> findByProductIdAndStatus(Long productId, LicenseStatus status);

    List<SoftwareLicense> findBySalespersonIdAndStatus(Long salespersonId, LicenseStatus status);

    /* Expiry / renewal queries */

    List<SoftwareLicense> findByExpiryDateBetween(LocalDateTime from, LocalDateTime to);

    List<SoftwareLicense> findByExpiryDateBefore(LocalDateTime dateTime);

    // Used by the daily scheduler: every licence that is not already renewed
    List<SoftwareLicense> findByStatusNot(LicenseStatus status);
}
