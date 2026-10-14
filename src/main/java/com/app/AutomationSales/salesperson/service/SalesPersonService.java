package com.app.AutomationSales.salesperson.service;

import com.app.AutomationSales.salesperson.dto.SalesPersonRequestDto;
import com.app.AutomationSales.salesperson.dto.SalesPersonResponseDto;

import java.util.List;

public interface SalesPersonService {

    SalesPersonResponseDto createSalesPerson(SalesPersonRequestDto request);

    List<SalesPersonResponseDto> findAllSalesPerson();

    SalesPersonResponseDto findSalesPersonById(Long id);

    SalesPersonResponseDto updateSalesPerson(SalesPersonRequestDto request, Long id);

    void deleteSalesPersonById(Long id);

    SalesPersonResponseDto findBySalesPersonEmail(String email);

    List<SalesPersonResponseDto> findByStatus(String status);

    List<SalesPersonResponseDto> findBySalesPersonRole(String role);

    //SalesPersonResponseDto LoginSalesPerson(String email, String password);


}
