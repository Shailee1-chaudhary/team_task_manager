package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.DashboardResponse;
import com.teamtask.entity.User;
import com.teamtask.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse> getDashboard(@AuthenticationPrincipal User currentUser) {
        DashboardResponse dashboard = dashboardService.getDashboard(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved successfully", dashboard));
    }
}
