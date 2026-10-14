package com.app.AutomationSales.appUser.passwordResetToken;

import com.app.AutomationSales.appUser.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "password_reset_tokens")
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime expiryTime;

    @Column(unique = true)
    private String token;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryTime);
    }
}
