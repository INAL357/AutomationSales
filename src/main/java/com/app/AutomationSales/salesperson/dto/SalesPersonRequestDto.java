package com.app.AutomationSales.salesperson.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SalesPersonRequestDto(

        @NotBlank(message = "salesPersonName is required")
        @Size(max = 100, message = "salesPersonName must be at most 100 characters")
        String salesPersonName,

        @NotBlank(message = "salesPersonEmail is required")
        @Email(message = "salesPersonEmail must be a valid email address")
        String salesPersonEmail,

        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "salesPersonNumber must be 7-20 digits (may include + - ( ) and spaces)")
        String salesPersonNumber,

        @NotBlank(message = "salesPersonRole is required")
        String salesPersonRole,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters")
        String password,

        String status
) {}
