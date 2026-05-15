package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.AuthResponse;
import com.teamtask.dto.LoginRequest;
import com.teamtask.dto.SignupRequest;
import com.teamtask.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("[AuthController] Signup request received for email: {}, name: {}, role: {}", 
                request.getEmail(), request.getName(), request.getRole());
        try {
            AuthResponse authResponse = authService.signup(request);
            log.info("[AuthController] Signup successful for email: {}, userId: {}", 
                    request.getEmail(), authResponse.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", authResponse));
        } catch (Exception e) {
            log.error("[AuthController] Signup failed for email: {} - {} - {}", 
                    request.getEmail(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("[AuthController] Login request received for email: {}", request.getEmail());
        try {
            AuthResponse authResponse = authService.login(request);
            log.info("[AuthController] Login successful for email: {}, userId: {}, role: {}", 
                    request.getEmail(), authResponse.getId(), authResponse.getRole());
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
        } catch (Exception e) {
            log.error("[AuthController] Login failed for email: {} - {} - {}", 
                    request.getEmail(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
