package com.teamtask.service;

import com.teamtask.dto.AuthResponse;
import com.teamtask.dto.LoginRequest;
import com.teamtask.dto.SignupRequest;
import com.teamtask.entity.Role;
import com.teamtask.entity.User;
import com.teamtask.exception.BadRequestException;
import com.teamtask.repository.UserRepository;
import com.teamtask.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        log.info("[AuthService] Processing signup for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[AuthService] Signup rejected - email already registered: {}", request.getEmail());
            throw new BadRequestException("Email is already registered");
        }

        Role role = Role.MEMBER;
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
                log.debug("[AuthService] Role parsed: {}", role);
            } catch (IllegalArgumentException e) {
                log.error("[AuthService] Invalid role provided: {}", request.getRole());
                throw new BadRequestException("Invalid role. Must be ADMIN or MEMBER");
            }
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        user = userRepository.save(user);
        log.info("[AuthService] User created successfully - id: {}, email: {}, role: {}", 
                user.getId(), user.getEmail(), user.getRole());

        String token = jwtUtil.generateToken(user);
        log.debug("[AuthService] JWT token generated for user: {} (token length: {})", 
                user.getEmail(), token.length());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("[AuthService] Processing login for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            log.debug("[AuthService] Authentication successful for: {}", request.getEmail());

            User user = (User) authentication.getPrincipal();
            String token = jwtUtil.generateToken(user);
            log.info("[AuthService] Login successful - userId: {}, email: {}, role: {}", 
                    user.getId(), user.getEmail(), user.getRole());

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();
        } catch (Exception e) {
            log.error("[AuthService] Authentication failed for email: {} - {} - {}", 
                    request.getEmail(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
