package com.app.AutomationSales.knowledgeDocument.service;

import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.knowledgeDocument.dto.KnowledgeDocumentRequestDto;
import com.app.AutomationSales.knowledgeDocument.dto.KnowledgeDocumentResponseDto;
import com.app.AutomationSales.knowledgeDocument.model.KnowledgeDocument;
import com.app.AutomationSales.knowledgeDocument.repository.KnowledgeDocumentRepository;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.softwarelicense.repository.SoftwareLicenseRepository;
import com.app.AutomationSales.softwareproduct.model.SoftwareProduct;
import com.app.AutomationSales.softwareproduct.repository.SoftwareProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final SoftwareProductRepository softwareProductRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;

    public KnowledgeDocumentServiceImpl(
            KnowledgeDocumentRepository knowledgeDocumentRepository,
            SoftwareProductRepository softwareProductRepository,
            SoftwareLicenseRepository softwareLicenseRepository) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.softwareProductRepository = softwareProductRepository;
        this.softwareLicenseRepository = softwareLicenseRepository;
    }

    @Override
    public KnowledgeDocumentResponseDto create(KnowledgeDocumentRequestDto request) {

        KnowledgeDocument document = new KnowledgeDocument();
        applyRequest(document, request);

        KnowledgeDocument savedDocument = knowledgeDocumentRepository.save(document);

        return toResponse(savedDocument);
    }

    @Override
    public List<KnowledgeDocumentResponseDto> findAll() {
        return knowledgeDocumentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public KnowledgeDocumentResponseDto findById(Long id) {
        return toResponse(findDocumentEntityById(id));
    }

    @Override
    public KnowledgeDocumentResponseDto update(KnowledgeDocumentRequestDto request, Long id) {

        KnowledgeDocument existingDocument = findDocumentEntityById(id);
        applyRequest(existingDocument, request);

        KnowledgeDocument updatedDocument = knowledgeDocumentRepository.save(existingDocument);

        return toResponse(updatedDocument);
    }

    @Override
    public void delete(Long id) {
        KnowledgeDocument existingDocument = findDocumentEntityById(id);
        knowledgeDocumentRepository.delete(existingDocument);
    }

    @Override
    public List<KnowledgeDocumentResponseDto> findByProductId(Long productId) {
        return knowledgeDocumentRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private KnowledgeDocument findDocumentEntityById(Long id) {
        return knowledgeDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Knowledge Document not found with id: " + id));
    }

    private void applyRequest(KnowledgeDocument document, KnowledgeDocumentRequestDto request) {
        document.setTitle(request.title());
        document.setDescription(request.description());
        document.setContent(request.content());
        document.setType(request.type());
        document.setDocumentUrl(request.documentUrl());
        document.setActive(request.active() == null || request.active());

        setProduct(document, request.productId());
        setSoftwareLicense(document, request.softwareLicenseId());
    }

    private void setProduct(KnowledgeDocument document, Long productId) {
        if (productId == null) {
            document.setProduct(null);
            return;
        }

        SoftwareProduct product = softwareProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareProduct not found with id: " + productId));

        document.setProduct(product);
    }

    private void setSoftwareLicense(KnowledgeDocument document, Long softwareLicenseId) {
        if (softwareLicenseId == null) {
            document.setSoftwareLicense(null);
            return;
        }

        SoftwareLicense license = softwareLicenseRepository.findById(softwareLicenseId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareLicense not found with id: " + softwareLicenseId));

        document.setSoftwareLicense(license);
    }

    private KnowledgeDocumentResponseDto toResponse(KnowledgeDocument document) {

        Long productId = document.getProduct() != null
                ? document.getProduct().getId()
                : null;

        Long softwareLicenseId = document.getSoftwareLicense() != null
                ? document.getSoftwareLicense().getId()
                : null;

        return new KnowledgeDocumentResponseDto(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getContent(),
                productId,
                softwareLicenseId,
                document.getType(),
                document.getDocumentUrl(),
                document.isActive(),
                document.getCreatedAt(),
                document.getModifiedAt()
        );
    }
}
