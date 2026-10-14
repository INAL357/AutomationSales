package com.app.AutomationSales.renewal.repository;

import com.app.AutomationSales.renewal.model.Renewal;
import com.app.AutomationSales.renewal.model.RenewalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RenewalRepository extends JpaRepository<Renewal, Long> {

    List<Renewal> findByLicenseId(Long licenseId);

    List<Renewal> findByStatus(RenewalStatus status);

    List<Renewal> findByCustomerId(Long customerId);

    // Renewals of licences that belong to this salesperson (renewal -> license -> salesperson -> id)
    List<Renewal> findByLicense_Salesperson_Id(Long salesPersonId);

    // The scheduler uses these to find the licence's current open renewal
    // and to skip licences whose renewal is already RENEWED / LOST.
    Optional<Renewal> findFirstByLicenseIdAndStatusInOrderByIdDesc(Long licenseId, Collection<RenewalStatus> statuses);

    boolean existsByLicenseIdAndStatusIn(Long licenseId, Collection<RenewalStatus> statuses);
}
