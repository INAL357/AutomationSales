package com.app.AutomationSales.appUser.UserRepository;

import com.app.AutomationSales.appUser.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsBySalesPersonId(Long salesPersonId);

    boolean existsByEmail(@NotBlank(message = "salesPersonEmail is required") @Email(message = "salesPersonEmail must be a valid email address") String s);
}
