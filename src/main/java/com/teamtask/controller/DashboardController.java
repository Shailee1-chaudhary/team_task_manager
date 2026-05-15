package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.DashboardResponse;
import com.teamtask.entity.User;
import com.teamtask.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse> getDashboard(@AuthenticationPrincipal User currentUser) {
        log.info("[DashboardController] GET /api/dashboard - User: {}", 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[DashboardController] GET /api/dashboard - currentUser is NULL! Authentication failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            DashboardResponse dashboard = dashboardService.getDashboard(currentUser);
            log.info("[DashboardController] Dashboard retrieved - projects: {}, totalTasks: {}, todo: {}, inProgress: {}, done: {}", 
                    dashboard.getTotalProjects(), dashboard.getTotalTasks(), 
                    dashboard.getTodoTasks(), dashboard.getInProgressTasks(), dashboard.getCompletedTasks());
            return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved successfully", dashboard));
        } catch (Exception e) {
            log.error("[DashboardController] Failed to get dashboard for user: {} - {} - {}", 
                    currentUser.getEmail(), e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
}
