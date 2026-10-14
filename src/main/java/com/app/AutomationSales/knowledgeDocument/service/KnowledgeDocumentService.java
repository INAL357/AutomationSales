package com.app.AutomationSales.knowledgeDocument.service;

import com.app.AutomationSales.knowledgeDocument.dto.KnowledgeDocumentRequestDto;
import com.app.AutomationSales.knowledgeDocument.dto.KnowledgeDocumentResponseDto;

import java.util.List;

public interface KnowledgeDocumentService {

    KnowledgeDocumentResponseDto create(KnowledgeDocumentRequestDto request);

    List<KnowledgeDocumentResponseDto> findAll();

    KnowledgeDocumentResponseDto findById(Long id);

    KnowledgeDocumentResponseDto update(KnowledgeDocumentRequestDto request, Long id);

    void delete(Long id);

    List<KnowledgeDocumentResponseDto> findByProductId(Long productId);
}
