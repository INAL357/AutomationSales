package com.app.AutomationSales.salestask.repository;

import com.app.AutomationSales.salestask.model.SalesTask;
import com.app.AutomationSales.salestask.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface SalesTaskRepository extends JpaRepository<SalesTask, Long> {

    List<SalesTask> findByLicenseId(Long licenseId);

    List<SalesTask> findByRenewalId(Long renewalId);

    List<SalesTask> findBySalespersonId(Long salespersonId);

    List<SalesTask> findByLeadId(Long leadId);

    List<SalesTask> findByCustomerId(Long customerId);

    List<SalesTask> findByTaskStatus(TaskStatus taskStatus);

    List<SalesTask> findBySalespersonIdAndTaskStatus(Long salespersonId, TaskStatus taskStatus);

    // Overdue = due date in the past and still OPEN / IN_PROGRESS
    List<SalesTask> findByDueDateBeforeAndTaskStatusIn(LocalDate date, Collection<TaskStatus> statuses);

    // Used when a renewal is closed: cancel its remaining open tasks
    List<SalesTask> findByRenewalIdAndTaskStatusIn(Long renewalId, Collection<TaskStatus> statuses);
}
