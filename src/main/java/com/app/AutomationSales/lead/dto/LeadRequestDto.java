package com.app.AutomationSales.lead.dto;

import com.app.AutomationSales.lead.model.LeadStatus;
import jakarta.validation.constraints.*;

public record LeadRequestDto(

        @NotBlank(message = "leadName is required")
        @Size(max = 100, message = "leadName must be at most 100 characters")
        String leadName,

        Long productId,

        Long salespersonId,

        @Size(max = 150, message = "companyName must be at most 150 characters")
        String companyName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "phone must be 7-20 digits (may include + - ( ) and spaces)")
        String phone,

        String source,

        @Size(max = 2000, message = "message must be at most 2000 characters")
        String message,

        LeadStatus status,

        @Min(value = 0, message = "leadScore must be between 0 and 100")
        @Max(value = 100, message = "leadScore must be between 0 and 100")
        Integer leadScore,

        @Positive(message = "requiredLicenses must be greater than 0")
        Integer requiredLicenses,

        @PositiveOrZero(message = "existingLicenses cannot be negative")
        Integer existingLicenses,

        @Positive(message = "preferredDurationMonths must be greater than 0")
        Integer preferredDurationMonths,

        String purchaseTimeline
) {}
