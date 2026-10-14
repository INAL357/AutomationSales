package com.app.AutomationSales.knowledgeDocument.controller;

import com.app.AutomationSales.knowledgeDocument.dto.KnowledgeDocumentRequestDto;
import com.app.AutomationSales.knowledgeDocument.dto.KnowledgeDocumentResponseDto;
import com.app.AutomationSales.knowledgeDocument.service.KnowledgeDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-documents")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService knowledgeDocumentService;

    public KnowledgeDocumentController(KnowledgeDocumentService knowledgeDocumentService) {
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    @PostMapping
    public ResponseEntity<KnowledgeDocumentResponseDto> create(@Valid @RequestBody KnowledgeDocumentRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(knowledgeDocumentService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeDocumentResponseDto>> findAll() {
        return ResponseEntity.ok(knowledgeDocumentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeDocumentResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(knowledgeDocumentService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeDocumentResponseDto> update(
            @Valid @RequestBody KnowledgeDocumentRequestDto request,
            @PathVariable Long id) {

        return ResponseEntity.ok(knowledgeDocumentService.update(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        knowledgeDocumentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<KnowledgeDocumentResponseDto>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(knowledgeDocumentService.findByProductId(productId));
    }
}
