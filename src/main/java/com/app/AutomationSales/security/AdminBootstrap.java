package com.app.AutomationSales.security;

import com.app.AutomationSales.appUser.UserRepository.UserRepository;
import com.app.AutomationSales.appUser.model.Role;
import com.app.AutomationSales.appUser.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN account when the users table is empty, so somebody can log in
 * and create the other users. Reads app.security.bootstrap-admin.* (ADMIN_EMAIL / ADMIN_PASSWORD).
 * Does nothing once any user exists.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.security.bootstrap-admin.email:}") String email,
                          @Value("${app.security.bootstrap-admin.password:}") String password,
                          @Value("${app.security.bootstrap-admin.full-name:Administrator}") String fullName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            log.warn("No users exist and ADMIN_EMAIL / ADMIN_PASSWORD are not set – nobody can log in yet.");
            return;
        }
        if (password.length() < 8) {
            log.warn("ADMIN_PASSWORD must be at least 8 characters – first admin was not created.");
            return;
        }
        userRepository.save(User.builder()
                .fullName(fullName)
                .email(email.trim().toLowerCase())
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .enabled(true)
                .build());
        log.info("Created first ADMIN user {}", email);
    }
}
