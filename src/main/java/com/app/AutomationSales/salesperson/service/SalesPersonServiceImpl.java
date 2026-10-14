package com.app.AutomationSales.salesperson.service;

import com.app.AutomationSales.appUser.UserRepository.UserRepository;
import com.app.AutomationSales.appUser.model.Role;
import com.app.AutomationSales.appUser.model.User;
import com.app.AutomationSales.exceptionHandling.BadRequestException;
import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.salesperson.dto.SalesPersonRequestDto;
import com.app.AutomationSales.salesperson.dto.SalesPersonResponseDto;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salesperson.repository.SalesPersonRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesPersonServiceImpl implements SalesPersonService {

    private final SalesPersonRepository salesPersonRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SalesPersonServiceImpl(SalesPersonRepository salesPersonRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        this.salesPersonRepository = salesPersonRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public SalesPersonResponseDto createSalesPerson(SalesPersonRequestDto request) {

        // 1. Email must not already exist (as a sales profile OR a login)
        if (salesPersonRepository.existsBySalesPersonEmail(request.salesPersonEmail())
                || userRepository.existsByEmail(request.salesPersonEmail())) {
            throw new IllegalArgumentException("A user with this email already exists");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new BadRequestException("password is required when creating a salesperson");
        }
        // 2. Create the sales profile
        SalesPerson salesPerson = new SalesPerson();
        salesPerson.setSalesPersonName(request.salesPersonName());
        salesPerson.setSalesPersonEmail(request.salesPersonEmail());
        salesPerson.setSalesPersonNumber(request.salesPersonNumber());
        salesPerson.setSalesPersonRole(request.salesPersonRole());
        salesPerson.setStatus("ACTIVE");
        salesPerson = salesPersonRepository.save(salesPerson);

        // 3. Create the login account
        // 3. Create the login account
        User user = new User();
        user.setFullName(request.salesPersonName());
        user.setEmail(request.salesPersonEmail());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.SALESPERSON);
        user.setEnabled(true);
        user.setSalesPerson(salesPerson);
        userRepository.save(user);

        return toResponse(salesPerson);
    }

    @Override
    public List<SalesPersonResponseDto> findAllSalesPerson() {
        return salesPersonRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SalesPersonResponseDto findSalesPersonById(Long id) {
        return toResponse(findSalesPersonEntityById(id));
    }

    @Override
    public SalesPersonResponseDto updateSalesPerson(SalesPersonRequestDto request, Long id) {

        SalesPerson existingSalesPerson = findSalesPersonEntityById(id);
        applyRequest(existingSalesPerson, request);

        SalesPerson updatedSalesPerson = salesPersonRepository.save(existingSalesPerson);

        return toResponse(updatedSalesPerson);
    }

    @Override
    public void deleteSalesPersonById(Long id) {
        SalesPerson existingSalesPerson = findSalesPersonEntityById(id);
        salesPersonRepository.delete(existingSalesPerson);
    }

    @Override
    public SalesPersonResponseDto findBySalesPersonEmail(String email) {
        return salesPersonRepository.findBySalesPersonEmail(email)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesPerson not found with email: " + email));
    }

    @Override
    public List<SalesPersonResponseDto> findByStatus(String status) {
        return salesPersonRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SalesPersonResponseDto> findBySalesPersonRole(String role) {
        return salesPersonRepository.findBySalesPersonRole(role)
                .stream()
                .map(this::toResponse)
                .toList();
    }
//
//    @Override
//    public SalesPersonResponseDto LoginSalesPerson(String email, String password) {
//        if(!salesPersonRepository.existsBySalesPersonEmail(email));
//    }

    private SalesPerson findSalesPersonEntityById(Long id) {
        return salesPersonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesPerson not found with id: " + id));
    }

    private void applyRequest(SalesPerson salesPerson, SalesPersonRequestDto request) {
        salesPerson.setSalesPersonName(request.salesPersonName());
        salesPerson.setSalesPersonEmail(request.salesPersonEmail());
        salesPerson.setSalesPersonNumber(request.salesPersonNumber());
        salesPerson.setSalesPersonRole(request.salesPersonRole());
        salesPerson.setStatus(request.status());
    }



    private SalesPersonResponseDto toResponse(SalesPerson salesPerson) {
        return new SalesPersonResponseDto(
                salesPerson.getId(),
                salesPerson.getSalesPersonName(),
                salesPerson.getSalesPersonEmail(),
                salesPerson.getSalesPersonNumber(),
                salesPerson.getSalesPersonRole(),
                salesPerson.getStatus(),
                salesPerson.getCreatedAt(),
                salesPerson.getModifiedAt()
        );
    }
}
