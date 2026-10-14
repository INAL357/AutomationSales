package com.app.AutomationSales.appUser.controller;

import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequestDto(
        @NotNull(message = "enabled is required")
        Boolean enabled) {
}
