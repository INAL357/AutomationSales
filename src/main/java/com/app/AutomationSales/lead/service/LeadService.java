package com.app.AutomationSales.lead.service;

import com.app.AutomationSales.lead.dto.LeadRequestDto;
import com.app.AutomationSales.lead.dto.LeadResponseDto;
import com.app.AutomationSales.lead.dto.LeadStatusUpdateRequestDto;
import com.app.AutomationSales.lead.dto.PublicLeadRequestDto;
import com.app.AutomationSales.lead.model.LeadStatus;

import java.util.List;

public interface LeadService {

    LeadResponseDto createLead(LeadRequestDto request);

    // Public website enquiry workflow.
    LeadResponseDto createWebsiteLead(PublicLeadRequestDto request);

    List<LeadResponseDto> findAllLeads();

    LeadResponseDto findLeadById(Long id);

    LeadResponseDto updateLead(LeadRequestDto request, Long id);

    LeadResponseDto updateLeadStatus(Long id, LeadStatusUpdateRequestDto request);

    void deleteLeadById(Long id);

    List<LeadResponseDto> findBySalespersonId(Long salesPersonId);

    List<LeadResponseDto> findByProductId(Long productId);

    List<LeadResponseDto> findByCompanyName(String companyName);

    List<LeadResponseDto> findByStatus(LeadStatus status);
}
