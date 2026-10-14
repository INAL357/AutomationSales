package com.app.AutomationSales.customer.service;

import com.app.AutomationSales.customer.dto.CustomerRequestDTO;
import com.app.AutomationSales.customer.dto.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerResponseDTO createCustomer(CustomerRequestDTO request);

    List<CustomerResponseDTO> findAllCustomers();

    CustomerResponseDTO findById(Long id);

    CustomerResponseDTO update(CustomerRequestDTO request, Long id);

    void delete(Long id);

    List<CustomerResponseDTO> findBySalespersonId(Long salesPersonId);
}
