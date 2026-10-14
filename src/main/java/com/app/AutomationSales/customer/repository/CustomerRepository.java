package com.app.AutomationSales.customer.repository;

import com.app.AutomationSales.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findBySalespersonId(Long salesPersonId);
}
