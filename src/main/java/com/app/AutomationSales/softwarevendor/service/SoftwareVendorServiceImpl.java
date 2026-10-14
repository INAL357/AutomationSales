package com.app.AutomationSales.softwarevendor.service;

import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.softwarevendor.dto.SoftwareVendorRequestDto;
import com.app.AutomationSales.softwarevendor.dto.SoftwareVendorResponseDto;
import com.app.AutomationSales.softwarevendor.model.SoftwareVendor;
import com.app.AutomationSales.softwarevendor.repository.SoftwareVendorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoftwareVendorServiceImpl implements SoftwareVendorService {

    private final SoftwareVendorRepository softwareVendorRepository;

    public SoftwareVendorServiceImpl(SoftwareVendorRepository softwareVendorRepository) {
        this.softwareVendorRepository = softwareVendorRepository;
    }

    @Override
    public SoftwareVendorResponseDto createSoftwareVendor(SoftwareVendorRequestDto request) {

        SoftwareVendor vendor = new SoftwareVendor();
        applyRequest(vendor, request);

        SoftwareVendor savedVendor = softwareVendorRepository.save(vendor);

        return toResponse(savedVendor);
    }

    @Override
    public List<SoftwareVendorResponseDto> findAll() {
        return softwareVendorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SoftwareVendorResponseDto findVendorById(Long id) {
        return toResponse(findVendorEntityById(id));
    }

    @Override
    public SoftwareVendorResponseDto updateVendor(SoftwareVendorRequestDto request, Long id) {

        SoftwareVendor existingVendor = findVendorEntityById(id);
        applyRequest(existingVendor, request);

        SoftwareVendor updatedVendor = softwareVendorRepository.save(existingVendor);

        return toResponse(updatedVendor);
    }

    @Override
    public void deleteVendor(Long id) {
        SoftwareVendor existingVendor = findVendorEntityById(id);
        softwareVendorRepository.delete(existingVendor);
    }

    @Override
    public List<SoftwareVendorResponseDto> findByVendorName(String vendorName) {

        List<SoftwareVendor> vendors = softwareVendorRepository.findByVendorName(vendorName);

        if (vendors.isEmpty()) {
            throw new ResourceNotFoundExceptionHandler("Vendor not found with name: " + vendorName);
        }

        return vendors.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SoftwareVendorResponseDto findByVendorEmail(String vendorEmail) {
        return softwareVendorRepository.findByVendorEmail(vendorEmail)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Vendor not found with email: " + vendorEmail));
    }

    @Override
    public SoftwareVendorResponseDto findByVendorPhone(String vendorPhone) {
        return softwareVendorRepository.findByVendorPhone(vendorPhone)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Vendor not found with phone: " + vendorPhone));
    }

    private SoftwareVendor findVendorEntityById(Long id) {
        return softwareVendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Vendor not found with id: " + id));
    }

    private void applyRequest(SoftwareVendor vendor, SoftwareVendorRequestDto request) {
        vendor.setVendorName(request.vendorName());
        vendor.setVendorDescription(request.vendorDescription());
        vendor.setVendorEmail(request.vendorEmail());
        vendor.setVendorPhone(request.vendorPhone());
    }

    private SoftwareVendorResponseDto toResponse(SoftwareVendor vendor) {
        return new SoftwareVendorResponseDto(
                vendor.getId(),
                vendor.getVendorName(),
                vendor.getVendorDescription(),
                vendor.getVendorEmail(),
                vendor.getVendorPhone(),
                vendor.getCreatedAt(),
                vendor.getModifiedAt()
        );
    }
}
