package com.helpdesk.backend.service;

import com.helpdesk.backend.dto.AuthRequest;
import com.helpdesk.backend.dto.AuthResponse;
import com.helpdesk.backend.dto.RegisterRequest;
import com.helpdesk.backend.dto.UserDTO;
import com.helpdesk.backend.entity.User;
import com.helpdesk.backend.entity.Role;
import com.helpdesk.backend.repository.UserRepository;
import com.helpdesk.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already taken");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .department(request.getDepartment())
                .status("ACTIVE")
                .build();
        userRepository.save(user);

        var jwtToken = jwtUtil.generateToken(user);
        
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .department(user.getDepartment())
                .build();

        return AuthResponse.builder()
                .token(jwtToken)
                .user(userDTO)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtUtil.generateToken(user);

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .department(user.getDepartment())
                .build();

        return AuthResponse.builder()
                .token(jwtToken)
                .user(userDTO)
                .build();
    }
}
