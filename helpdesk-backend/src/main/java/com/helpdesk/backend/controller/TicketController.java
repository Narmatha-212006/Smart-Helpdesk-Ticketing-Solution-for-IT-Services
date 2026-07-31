package com.helpdesk.backend.controller;

import com.helpdesk.backend.dto.TicketDTO;
import com.helpdesk.backend.entity.TicketStatus;
import com.helpdesk.backend.service.DashboardService;
import com.helpdesk.backend.service.TicketService;
import com.helpdesk.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final DashboardService dashboardService;

    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(
            @RequestBody TicketDTO request, 
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.createTicket(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/my")
    public ResponseEntity<List<TicketDTO>> getMyTickets(Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketsByCreator(authentication.getName()));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<TicketDTO>> getAssignedTickets(Authentication authentication) {
        return ResponseEntity.ok(ticketService.getAssignedTickets(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketDTO> updateTicketStatus(
            @PathVariable Long id, 
            @RequestParam TicketStatus status) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, status));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketDTO> assignTicket(
            @PathVariable Long id, 
            @RequestParam Long agentId) {
        return ResponseEntity.ok(ticketService.assignTicket(id, agentId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TicketDTO>> getRecentTickets(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        String email = authentication.getName();
        String roleFilter = userService.findByEmail(email).getRole().name().toLowerCase();
        return ResponseEntity.ok(dashboardService.getRecentTickets(email, roleFilter, limit));
    }
}
