package com.helpdesk.backend.controller;

import com.helpdesk.backend.dto.CreateUserRequest;
import com.helpdesk.backend.dto.UserDTO;
import com.helpdesk.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest dto) {
        return ResponseEntity.ok(userService.createAdminUser(dto));
    }
}
