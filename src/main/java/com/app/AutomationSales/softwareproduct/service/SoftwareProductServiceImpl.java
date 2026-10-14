package com.app.AutomationSales.softwareproduct.service;

import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.softwareproduct.dto.SoftwareProductRequestDto;
import com.app.AutomationSales.softwareproduct.dto.SoftwareProductResponseDto;
import com.app.AutomationSales.softwareproduct.model.SoftwareProduct;
import com.app.AutomationSales.softwareproduct.repository.SoftwareProductRepository;
import com.app.AutomationSales.softwarevendor.model.SoftwareVendor;
import com.app.AutomationSales.softwarevendor.repository.SoftwareVendorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoftwareProductServiceImpl implements SoftwareProductService {

    private final SoftwareProductRepository softwareProductRepository;
    private final SoftwareVendorRepository softwareVendorRepository;

    public SoftwareProductServiceImpl(
            SoftwareProductRepository softwareProductRepository,
            SoftwareVendorRepository softwareVendorRepository) {
        this.softwareProductRepository = softwareProductRepository;
        this.softwareVendorRepository = softwareVendorRepository;
    }

    @Override
    public SoftwareProductResponseDto createSoftwareProduct(SoftwareProductRequestDto request) {

        SoftwareProduct product = new SoftwareProduct();
        applyRequest(product, request);

        SoftwareProduct savedProduct = softwareProductRepository.save(product);

        return toResponse(savedProduct);
    }

    @Override
    public List<SoftwareProductResponseDto> findAll() {
        return softwareProductRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SoftwareProductResponseDto findProductId(Long id) {
        return toResponse(findProductEntityById(id));
    }

    @Override
    public SoftwareProductResponseDto updateProduct(SoftwareProductRequestDto request, Long id) {

        SoftwareProduct existingProduct = findProductEntityById(id);
        applyRequest(existingProduct, request);

        SoftwareProduct updatedProduct = softwareProductRepository.save(existingProduct);

        return toResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        SoftwareProduct existingProduct = findProductEntityById(id);
        softwareProductRepository.delete(existingProduct);
    }

    @Override
    public List<SoftwareProductResponseDto> findByProductName(String productName) {
        return softwareProductRepository.findByProductName(productName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SoftwareProductResponseDto> findByCategory(String category) {
        return softwareProductRepository.findByCategory(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SoftwareProductResponseDto> findByVendorId(Long vendorId) {
        return softwareProductRepository.findByVendorId(vendorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SoftwareProduct findProductEntityById(Long id) {
        return softwareProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareProduct not found with id: " + id));
    }

    private void applyRequest(SoftwareProduct product, SoftwareProductRequestDto request) {
        product.setProductName(request.productName());
        product.setProductDescription(request.productDescription());
        product.setProductPrice(request.productPrice());
        product.setCategory(request.category());
        product.setVersion(request.version());
        product.setPlatform(request.platform());
        product.setActive(request.active() == null || request.active());

        setVendor(product, request.vendorId());
    }

    private void setVendor(SoftwareProduct product, Long vendorId) {
        if (vendorId == null) {
            product.setVendor(null);
            return;
        }

        SoftwareVendor vendor = softwareVendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Vendor not found with id: " + vendorId));

        product.setVendor(vendor);
    }

    private SoftwareProductResponseDto toResponse(SoftwareProduct product) {

        Long vendorId = product.getVendor() != null
                ? product.getVendor().getId()
                : null;

        return new SoftwareProductResponseDto(
                product.getId(),
                vendorId,
                product.getProductName(),
                product.getProductDescription(),
                product.getProductPrice(),
                product.getCategory(),
                product.getVersion(),
                product.getPlatform(),
                product.isActive(),
                product.getCreatedAt(),
                product.getModifiedAt()
        );
    }
}
