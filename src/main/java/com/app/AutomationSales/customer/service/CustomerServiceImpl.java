package com.app.AutomationSales.customer.service;

import com.app.AutomationSales.customer.dto.CustomerRequestDTO;
import com.app.AutomationSales.customer.dto.CustomerResponseDTO;
import com.app.AutomationSales.customer.model.Customer;
import com.app.AutomationSales.customer.repository.CustomerRepository;
import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salesperson.repository.SalesPersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SalesPersonRepository salesPersonRepository;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            SalesPersonRepository salesPersonRepository) {
        this.customerRepository = customerRepository;
        this.salesPersonRepository = salesPersonRepository;
    }

    @Override
    public CustomerResponseDTO createCustomer(CustomerRequestDTO request) {

        Customer customer = new Customer();

        customer.setCustomerName(request.customerName());
        customer.setCustomerEmail(request.customerEmail());
        customer.setCustomerPhone(request.customerPhone());
        customer.setCompanyName(request.companyName());
        customer.setStatus(request.status());

        setSalesperson(customer, request.salespersonId());

        Customer savedCustomer = customerRepository.save(customer);

        return toResponse(savedCustomer);
    }

    @Override
    public List<CustomerResponseDTO> findAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CustomerResponseDTO findById(Long id) {
        Customer customer = findCustomerEntityById(id);
        return toResponse(customer);
    }

    @Override
    public CustomerResponseDTO update(CustomerRequestDTO request, Long id) {

        Customer existingCustomer = findCustomerEntityById(id);

        existingCustomer.setCustomerName(request.customerName());
        existingCustomer.setCustomerEmail(request.customerEmail());
        existingCustomer.setCustomerPhone(request.customerPhone());
        existingCustomer.setCompanyName(request.companyName());
        existingCustomer.setStatus(request.status());

        setSalesperson(existingCustomer, request.salespersonId());

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return toResponse(updatedCustomer);
    }

    @Override
    public void delete(Long id) {
        Customer existingCustomer = findCustomerEntityById(id);
        customerRepository.delete(existingCustomer);
    }

    @Override
    public List<CustomerResponseDTO> findBySalespersonId(Long salesPersonId) {
        return customerRepository.findBySalespersonId(salesPersonId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Customer findCustomerEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "Customer not found with id: " + id));
    }

    private void setSalesperson(Customer customer, Long salespersonId) {
        if (salespersonId == null) {
            customer.setSalesperson(null);
            return;
        }

        SalesPerson salesperson = salesPersonRepository.findById(salespersonId)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                        "SalesPerson not found with id: " + salespersonId));

        customer.setSalesperson(salesperson);
    }

    private CustomerResponseDTO toResponse(Customer customer) {

        Long salespersonId = customer.getSalesperson() != null
                ? customer.getSalesperson().getId()
                : null;

        return new CustomerResponseDTO(
                customer.getId(),
                customer.getCustomerName(),
                customer.getCustomerEmail(),
                customer.getCustomerPhone(),
                customer.getCompanyName(),
                salespersonId,
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getModifiedAt()
        );
    }
}
