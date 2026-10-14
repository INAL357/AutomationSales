package com.app.AutomationSales.knowledgeDocument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** active defaults to true when not sent. */
public record KnowledgeDocumentRequestDto(

        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @Size(max = 1000, message = "description must be at most 1000 characters")
        String description,

        String content,

        Long productId,

        Long softwareLicenseId,

        String type,

        @Size(max = 500, message = "documentUrl must be at most 500 characters")
        String documentUrl,

        Boolean active
) {}
