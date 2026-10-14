package com.app.AutomationSales.salestask.service;

import com.app.AutomationSales.salestask.dto.SalesTaskRequestDto;
import com.app.AutomationSales.salestask.dto.SalesTaskResponseDto;
import com.app.AutomationSales.salestask.model.TaskStatus;

import java.util.List;

public interface SalesTaskService {

    SalesTaskResponseDto createSalesTask(SalesTaskRequestDto request);

    List<SalesTaskResponseDto> findAllTasks();

    SalesTaskResponseDto findTasksById(Long id);

    SalesTaskResponseDto updateTask(SalesTaskRequestDto request, Long id);

    SalesTaskResponseDto updateTaskStatus(Long id, TaskStatus taskStatus);

    void deleteTasksById(Long id);

    List<SalesTaskResponseDto> findByLicenseId(Long licenseId);

    List<SalesTaskResponseDto> findByRenewalId(Long renewalId);

    List<SalesTaskResponseDto> findBySalespersonId(Long salespersonId);

    List<SalesTaskResponseDto> findByLeadId(Long leadId);

    List<SalesTaskResponseDto> findByCustomerId(Long customerId);

    List<SalesTaskResponseDto> findByTaskStatus(TaskStatus taskStatus);

    List<SalesTaskResponseDto> findBySalespersonIdAndTaskStatus(Long salespersonId, TaskStatus taskStatus);

    List<SalesTaskResponseDto> findOverdueTasks();
}
