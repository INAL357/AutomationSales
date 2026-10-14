package com.app.AutomationSales.appUser.UserDto;

import com.app.AutomationSales.appUser.model.Role;
import com.app.AutomationSales.appUser.model.User;

import java.time.Instant;

public record UserResponseDto(Long userId,
                              String fullName,
                              String email,
                              Role role,
                              Long salesPersonId,
                              boolean enabled,
                              Instant createdAt,
                              Instant lastLoginAt) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getSalesPerson() != null ? user.getSalesPerson().getId() : null,
                user.isEnabled(),
                user.getCreatedAt(),
                user.getLogIn()
        );
    }
}
