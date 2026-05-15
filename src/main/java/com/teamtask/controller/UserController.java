package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.UserSummary;
import com.teamtask.entity.User;
import com.teamtask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        log.info("[UserController] GET /api/users/me - User: {}", 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[UserController] GET /api/users/me - currentUser is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        UserSummary summary = UserSummary.builder()
                .id(currentUser.getId())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .role(currentUser.getRole().name())
                .build();
        log.info("[UserController] Current user retrieved - id: {}, email: {}, role: {}", 
                currentUser.getId(), currentUser.getEmail(), currentUser.getRole());
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved", summary));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllUsers() {
        log.info("[UserController] GET /api/users - Fetching all users (admin only)");
        List<UserSummary> users = userRepository.findAll().stream()
                .map(user -> UserSummary.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .collect(Collectors.toList());
        log.info("[UserController] Retrieved {} users", users.size());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllUsersForAssignment() {
        log.info("[UserController] GET /api/users/all - Fetching all users for assignment");
        List<UserSummary> users = userRepository.findAll().stream()
                .map(user -> UserSummary.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .collect(Collectors.toList());
        log.info("[UserController] Retrieved {} users for assignment", users.size());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }
}
