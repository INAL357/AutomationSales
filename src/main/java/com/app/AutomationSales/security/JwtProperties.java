package com.app.AutomationSales.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.jwt.* in application.yaml
 *
 * @param secret            Base64-encoded HMAC key, at least 32 bytes (256 bits) once decoded.
 *                          Set it with the JWT_SECRET environment variable outside of local dev.
 * @param expirationMinutes how long a token stays valid after login
 * @param issuer            written into the token and checked on every request
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes, String issuer) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret is not set (use the JWT_SECRET environment variable)");
        }
        if (expirationMinutes <= 0) {
            expirationMinutes = 60;
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "AutomationSales";
        }
    }
}
