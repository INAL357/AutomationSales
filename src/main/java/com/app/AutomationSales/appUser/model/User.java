package com.app.AutomationSales.appUser.model;

import com.app.AutomationSales.salesperson.model.SalesPerson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 50)
    private String fullName;

    // Used as the login username, so it must be unique
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // BCrypt hash (always 60 chars) – never the plain password
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // A SALESPERSON login is linked to exactly one SalesPerson record (null for ADMIN)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id", unique = true)
    private SalesPerson salesPerson;

    @Column(nullable = false)
    private boolean enabled;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "last_login_at")
    private Instant logIn;
}
