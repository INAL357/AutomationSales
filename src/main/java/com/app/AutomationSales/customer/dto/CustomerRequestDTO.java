package com.app.AutomationSales.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequestDTO(

        @NotBlank(message = "customerName is required")
        @Size(max = 100, message = "customerName must be at most 100 characters")
        String customerName,

        @NotBlank(message = "customerEmail is required")
        @Email(message = "customerEmail must be a valid email address")
        String customerEmail,

        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "customerPhone must be 7-20 digits (may include + - ( ) and spaces)")
        String customerPhone,

        @NotBlank(message = "companyName is required")
        @Size(max = 150, message = "companyName must be at most 150 characters")
        String companyName,

        Long salespersonId,

        String status
) {}
