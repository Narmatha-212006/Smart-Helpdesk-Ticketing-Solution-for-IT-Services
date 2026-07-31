package com.helpdesk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDTO {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long closedTickets;
    private long resolvedToday;
    private double avgResolutionDays;
    private long pendingTickets;
    private long highPriority;
}
