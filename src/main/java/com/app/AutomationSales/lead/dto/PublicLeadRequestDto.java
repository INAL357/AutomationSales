package com.app.AutomationSales.lead.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PublicLeadRequestDto(

        @NotBlank(message = "leadName is required")
        @Size(max = 100, message = "leadName must be at most 100 characters")
        String leadName,

        @NotBlank(message = "companyName is required")
        @Size(max = 150, message = "companyName must be at most 150 characters")
        String companyName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        @NotBlank(message = "phone is required")
        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "phone must be 7-20 digits (may include + - ( ) and spaces)")
        String phone,

        @NotNull(message = "productId is required")
        Long productId,

        @Size(max = 2000, message = "message must be at most 2000 characters")
        String message
) {}
