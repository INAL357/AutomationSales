package com.app.AutomationSales.salescall.repository;

import com.app.AutomationSales.salescall.model.SalesCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesCallRepository extends JpaRepository<SalesCall, Long> {

    List<SalesCall> findBySalespersonId(Long salespersonId);

    List<SalesCall> findByCustomerId(Long customerId);

    List<SalesCall> findByLeadId(Long leadId);

    List<SalesCall> findByRenewalId(Long renewalId);

    List<SalesCall> findByLicenseId(Long licenseId);

    // Replaces the old exact-match findByStartTime / findByEndTime on SalesTask
    List<SalesCall> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
}
