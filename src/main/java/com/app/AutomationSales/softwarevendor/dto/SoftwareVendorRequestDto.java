package com.app.AutomationSales.softwarevendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SoftwareVendorRequestDto(

        @NotBlank(message = "vendorName is required")
        @Size(max = 150, message = "vendorName must be at most 150 characters")
        String vendorName,

        @Size(max = 1000, message = "vendorDescription must be at most 1000 characters")
        String vendorDescription,

        @Email(message = "vendorEmail must be a valid email address")
        String vendorEmail,

        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "vendorPhone must be 7-20 digits (may include + - ( ) and spaces)")
        String vendorPhone
) {}
