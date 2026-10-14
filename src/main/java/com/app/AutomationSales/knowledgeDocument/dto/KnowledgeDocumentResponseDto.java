package com.app.AutomationSales.knowledgeDocument.dto;

import java.time.Instant;

public record KnowledgeDocumentResponseDto(
        Long documentId,
        String title,
        String description,
        String content,
        Long productId,
        Long softwareLicenseId,
        String type,
        String documentUrl,
        boolean active,
        Instant createdAt,
        Instant modifiedAt
) {}
