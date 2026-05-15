package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.ProjectRequest;
import com.teamtask.dto.ProjectResponse;
import com.teamtask.entity.User;
import com.teamtask.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal User currentUser) {
        ProjectResponse project = projectService.createProject(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", project));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProjects(@AuthenticationPrincipal User currentUser) {
        List<ProjectResponse> projects = projectService.getAllProjects(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        ProjectResponse project = projectService.getProjectById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal User currentUser) {
        ProjectResponse project = projectService.updateProject(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse> addMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        ProjectResponse project = projectService.addMember(projectId, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Member added successfully", project));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        ProjectResponse project = projectService.removeMember(projectId, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", project));
    }
}
