package com.app.AutomationSales.lead.repository;

import com.app.AutomationSales.lead.model.Lead;
import com.app.AutomationSales.lead.model.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findBySalespersonId(Long salesPersonId);

    List<Lead> findByProductId(Long productId);

    List<Lead> findByCompanyName(String companyName);

    List<Lead> findByStatus(LeadStatus status);

    long countBySalespersonId(Long salesPersonId);
}
