package com.helpdesk.backend.controller;

import com.helpdesk.backend.dto.DashboardSummaryDTO;
import com.helpdesk.backend.entity.Role;
import com.helpdesk.backend.service.DashboardService;
import com.helpdesk.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping("/user-stats")
    public ResponseEntity<DashboardSummaryDTO> getUserStats(Authentication authentication) {
        String email = authentication.getName();
        String roleFilter = userService.findByEmail(email).getRole().name().toLowerCase();
        return ResponseEntity.ok(dashboardService.getSummary(email, roleFilter));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategoryStats(Authentication authentication) {
        String email = authentication.getName();
        String roleFilter = userService.findByEmail(email).getRole().name().toLowerCase();
        return ResponseEntity.ok(dashboardService.getCategoryStats(email, roleFilter));
    }

    @GetMapping("/volume")
    public ResponseEntity<?> getVolumeStats(Authentication authentication) {
        String email = authentication.getName();
        String roleFilter = userService.findByEmail(email).getRole().name().toLowerCase();
        return ResponseEntity.ok(dashboardService.getTicketVolumeLast14Days(email, roleFilter));
    }
}

