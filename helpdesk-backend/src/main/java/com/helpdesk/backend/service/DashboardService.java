package com.helpdesk.backend.service;

import com.helpdesk.backend.dto.*;
import com.helpdesk.backend.entity.Role;
import com.helpdesk.backend.entity.Ticket;
import com.helpdesk.backend.entity.TicketStatus;
import com.helpdesk.backend.entity.User;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public DashboardSummaryDTO getSummary(String email, String roleFilter) {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Ticket> tickets = getFilteredTickets(roleFilter, user);

        long total = tickets.size();
        long open = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgress = tickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long closed = tickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED || t.getStatus() == TicketStatus.RESOLVED).count();
        long highPriority = tickets.stream().filter(t -> t.getPriority() != null && (t.getPriority().equals(com.helpdesk.backend.entity.TicketPriority.HIGH) || t.getPriority().equals(com.helpdesk.backend.entity.TicketPriority.CRITICAL)) && t.getStatus() != TicketStatus.CLOSED && t.getStatus() != TicketStatus.RESOLVED).count();
        long pending = open + inProgress;

        LocalDate today = LocalDate.now();
        long resolvedToday = tickets.stream()
                .filter(t -> (t.getStatus() == TicketStatus.CLOSED || t.getStatus() == TicketStatus.RESOLVED) && t.getCreatedAt().toLocalDate().equals(today))
                .count();

        double avgResolution = calculateAvgResolutionDays(tickets);

        return DashboardSummaryDTO.builder()
                .totalTickets(total)
                .openTickets(open)
                .inProgressTickets(inProgress)
                .closedTickets(closed)
                .resolvedToday(resolvedToday)
                .avgResolutionDays(avgResolution)
                .pendingTickets(pending)
                .highPriority(highPriority)
                .build();
    }

    public List<TicketDTO> getRecentTickets(String email, String roleFilter, int limit) {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Ticket> tickets = getFilteredTickets(roleFilter, user)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .toList();
        return tickets.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CategoryStatDTO> getCategoryStats(String email, String roleFilter) {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Ticket> tickets = getFilteredTickets(roleFilter, user);
        Map<String, Long> stats = tickets.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getCategory() != null ? t.getCategory() : "Other",
                    Collectors.counting()
                ));
        return stats.entrySet().stream()
                .map(e -> CategoryStatDTO.builder().category(e.getKey()).count(e.getValue()).build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();
    }

    public List<TicketVolumeDTO> getTicketVolumeLast14Days(String email, String roleFilter) {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Ticket> tickets = getFilteredTickets(roleFilter, user);
        Map<LocalDate, Long> volume = tickets.stream()
                .filter(t -> t.getCreatedAt().toLocalDate().isAfter(LocalDate.now().minusDays(14)))
                .collect(Collectors.groupingBy(
                    t -> t.getCreatedAt().toLocalDate(),
                    Collectors.counting()
                ));

        return LocalDate.now().datesUntil(LocalDate.now().plusDays(1))
                .map(date -> TicketVolumeDTO.builder()
                        .date(date)
                        .count(volume.getOrDefault(date, 0L))
                        .build())
                .toList();
    }

    private List<Ticket> getFilteredTickets(String roleFilter, User currentUser) {
        return switch (roleFilter.toLowerCase()) {
            case "user" -> ticketRepository.findByCreatedBy(currentUser);
            case "agent" -> ticketRepository.findByAssignedTo(currentUser);
            case "admin" -> ticketRepository.findAll();
            default -> ticketRepository.findAll();
        };
    }

    private double calculateAvgResolutionDays(List<Ticket> tickets) {
        // Simplified: avg days for closed tickets from create to now (improve with resolved date field later)
        List<Ticket> resolved = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CLOSED || t.getStatus() == TicketStatus.RESOLVED)
                .toList();
        if (resolved.isEmpty()) return 0.0;
        double sumDays = resolved.stream()
                .mapToDouble(t -> LocalDateTime.now().toLocalDate().toEpochDay() - t.getCreatedAt().toLocalDate().toEpochDay())
                .sum();
        return Math.round(sumDays / resolved.size() * 10.0) / 10.0;
    }

    private TicketDTO mapToDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .createdBy(mapUserDTO(ticket.getCreatedBy()))
                .assignedTo(ticket.getAssignedTo() != null ? mapUserDTO(ticket.getAssignedTo()) : null)
                .createdAt(ticket.getCreatedAt())
                .build();
    }

    private UserDTO mapUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
