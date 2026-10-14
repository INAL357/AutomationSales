package com.app.AutomationSales.knowledgeDocument.repository;

import com.app.AutomationSales.knowledgeDocument.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByProductId(Long productId);
}
