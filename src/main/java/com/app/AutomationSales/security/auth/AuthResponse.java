package com.app.AutomationSales.security.auth;

import com.app.AutomationSales.appUser.UserDto.UserResponseDto;

/**
 * Returned by POST /api/auth/login, e.g.
 * { "accessToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 3600, "user": { ... } }
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponseDto user
) {
}
