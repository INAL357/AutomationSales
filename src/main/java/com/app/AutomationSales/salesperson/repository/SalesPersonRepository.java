package com.app.AutomationSales.salesperson.repository;

import com.app.AutomationSales.salesperson.model.SalesPerson;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesPersonRepository extends JpaRepository<SalesPerson, Long> {
    Optional<SalesPerson> findBySalesPersonEmail(String SalesPersonEmail);

    List<SalesPerson> findByStatus(String status);

    List<SalesPerson> findBySalesPersonRole(String role);



    boolean existsBySalesPersonEmail(@NotBlank(message = "salesPersonEmail is required") @Email(message = "salesPersonEmail must be a valid email address") String s);
}
