package com.app.AutomationSales.softwarelicense.service;

import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.customer.repository.CustomerRepository;
import com.app.AutomationSales.exceptionHandling.BadRequestException;
import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salesperson.repository.SalesPersonRepository;
import com.app.AutomationSales.softwarelicense.dto.SoftwareLicenseRequestDto;
import com.app.AutomationSales.softwarelicense.dto.SoftwareLicenseResponseDto;
import com.app.AutomationSales.softwarelicense.model.LicenseStatus;
import com.app.AutomationSales.softwarelicense.model.SoftwareLicense;
import com.app.AutomationSales.softwarelicense.repository.SoftwareLicenseRepository;
import com.app.AutomationSales.softwareproduct.model.SoftwareProduct;
import com.app.AutomationSales.softwareproduct.repository.SoftwareProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SoftwareLicenseServiceImpl implements SoftwareLicenseService {

    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final SoftwareProductRepository softwareProductRepository;
    private final CustomerRepository customerRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final LicenseExpiryCalculator expiryCalculator;

    public SoftwareLicenseServiceImpl(
            SoftwareLicenseRepository softwareLicenseRepository,
            SoftwareProductRepository softwareProductRepository,
            CustomerRepository customerRepository,
            SalesPersonRepository salesPersonRepository,
            LicenseExpiryCalculator expiryCalculator) {
        this.softwareLicenseRepository = softwareLicenseRepository;
        this.softwareProductRepository = softwareProductRepository;
        this.customerRepository = customerRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.expiryCalculator = expiryCalculator;
    }

    @Override
    public SoftwareLicenseResponseDto createSoftwareLicense(SoftwareLicenseRequestDto request) {

        SoftwareLicense license = new SoftwareLicense();
        applyRequest(license, request);

        SoftwareLicense savedLicense = softwareLicenseRepository.save(license);

        return toResponse(savedLicense);
    }

    @Override
    public List<SoftwareLicenseResponseDto> findAllSoftwareLicense() {
        return toResponseList(softwareLicenseRepository.findAll());
    }

    @Override
    public SoftwareLicenseResponseDto findLicenseById(Long id) {
        return toResponse(findLicenseEntityById(id));
    }

    @Override
    public SoftwareLicenseResponseDto updateSoftwareLicense(SoftwareLicenseRequestDto request, Long id) {

        SoftwareLicense existingLicense = findLicenseEntityById(id);
        applyRequest(existingLicense, request);

        SoftwareLicense updatedLicense = softwareLicenseRepository.save(existingLicense);

        return toResponse(updatedLicense);
    }

    @Override
    public void deleteSoftwareLicense(Long id) {
        SoftwareLicense existingLicense = findLicenseEntityById(id);
        softwareLicenseRepository.delete(existingLicense);
    }

    @Override
    public List<SoftwareLicenseResponseDto> findByCustomerId(Long customerId) {
        return toResponseList(softwareLicenseRepository.findByCustomerId(customerId));
    }

    @Override
    public List<SoftwareLicenseResponseDto> findByProductId(Long productId) {
        return toResponseList(softwareLicenseRepository.findByProductId(productId));
    }

    @Override
    public List<SoftwareLicenseResponseDto> findBySalespersonId(Long salespersonId) {
        return toResponseList(softwareLicenseRepository.findBySalespersonId(salespersonId));
    }

    @Override
    public List<SoftwareLicenseResponseDto> findByStatus(LicenseStatus status) {
        return toResponseList(softwareLicenseRepository.findByStatus(status));
    }

    @Override
    public List<SoftwareLicenseResponseDto> findByCustomerIdAndStatus(Long customerId, LicenseStatus status) {
        return toResponseList(softwareLicenseRepository.findByCustomerIdAndStatus(customerId, status));
    }

    @Override
    public List<SoftwareLicenseResponseDto> findByProductIdAndStatus(Long productId, LicenseStatus status) {
        return toResponseList(softwareLicenseRepository.findByProductIdAndStatus(productId, status));
    }

    @Override
    public List<SoftwareLicenseResponseDto> findBySalespersonIdAndStatus(Long salespersonId, LicenseStatus status) {
        return toResponseList(softwareLicenseRepository.findBySalespersonIdAndStatus(salespersonId, status));
    }

    /* ---------- Expiry / renewal logic ---------- */

    @Override
    public List<SoftwareLicenseResponseDto> getLicensesExpiringWithinDays(int days) {

        if (days < 0) {
            throw new BadRequestException("days must be 0 or more");
        }

        // From the start of today until the end of the day "days" from now
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = LocalDate.now().plusDays(days).atTime(LocalTime.MAX);

        return softwareLicenseRepository.findByExpiryDateBetween(from, to)
                .stream()
                .filter(license -> license.getStatus() != LicenseStatus.RENEWED)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SoftwareLicenseResponseDto> getExpiredLicenses() {

        // Anything that expired before today and was not renewed
        return softwareLicenseRepository.findByExpiryDateBefore(LocalDate.now().atStartOfDay())
                .stream()
                .filter(license -> license.getStatus() != LicenseStatus.RENEWED)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public boolean isExpiringSoon(Long licenseId, int days) {
        SoftwareLicense license = findLicenseEntityById(licenseId);
        return expiryCalculator.isExpiringWithin(license, days);
    }

    /* ---------- Helpers ---------- */

    private SoftwareLicense findLicenseEntityById(Long id) {
        return softwareLicenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareLicense not found with id: " + id));
    }

    private void applyRequest(SoftwareLicense license, SoftwareLicenseRequestDto request) {

        LocalDateTime purchaseDate = request.purchaseDate();
        LocalDateTime expiryDate = request.expiryDate();
        Integer durationMonths = request.durationMonths();

        // 1. Work out the expiry date / duration
        if (expiryDate == null && durationMonths == null) {
            throw new BadRequestException("Send either expiryDate or durationMonths");
        }
        if (expiryDate == null) {
            expiryDate = purchaseDate.plusMonths(durationMonths);
        }
        if (!expiryDate.isAfter(purchaseDate)) {
            throw new BadRequestException("expiryDate must be after purchaseDate");
        }
        if (durationMonths == null) {
            durationMonths = (int) ChronoUnit.MONTHS.between(purchaseDate, expiryDate);
        }

        license.setLicenseQuantity(request.licenseQuantity());
        license.setPurchaseDate(purchaseDate);
        license.setExpiryDate(expiryDate);
        license.setDurationMonths(durationMonths);

        // 2. Relationships
        setProduct(license, request.productId());
        setCustomer(license, request.customerId());
        setSalesperson(license, request.salespersonId());

        // 3. Status is always calculated, never sent by the client
        license.setStatus(expiryCalculator.calculateStatus(license));
    }

    private void setProduct(SoftwareLicense license, Long productId) {
        SoftwareProduct product = softwareProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SoftwareProduct not found with id: " + productId));

        license.setProduct(product);
    }

    private void setCustomer(SoftwareLicense license, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Customer not found with id: " + customerId));

        license.setCustomer(customer);
    }

    private void setSalesperson(SoftwareLicense license, Long salespersonId) {
        if (salespersonId == null) {
            license.setSalesperson(null);
            return;
        }

        SalesPerson salesperson = salesPersonRepository.findById(salespersonId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesPerson not found with id: " + salespersonId));

        license.setSalesperson(salesperson);
    }

    private List<SoftwareLicenseResponseDto> toResponseList(List<SoftwareLicense> licenses) {
        return licenses.stream()
                .map(this::toResponse)
                .toList();
    }

    private SoftwareLicenseResponseDto toResponse(SoftwareLicense license) {

        Long productId = license.getProduct() != null
                ? license.getProduct().getId()
                : null;

        Long customerId = license.getCustomer() != null
                ? license.getCustomer().getId()
                : null;

        Long salespersonId = license.getSalesperson() != null
                ? license.getSalesperson().getId()
                : null;

        return new SoftwareLicenseResponseDto(
                license.getId(),
                productId,
                customerId,
                salespersonId,
                license.getLicenseQuantity(),
                license.getDurationMonths(),
                license.getPurchaseDate(),
                license.getExpiryDate(),
                expiryCalculator.daysRemaining(license.getExpiryDate()),
                license.getStatus(),
                license.getCreatedAt(),
                license.getModifiedAt()
        );
    }
}
