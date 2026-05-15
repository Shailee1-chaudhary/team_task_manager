package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.ProjectRequest;
import com.teamtask.dto.ProjectResponse;
import com.teamtask.entity.User;
import com.teamtask.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("[ProjectController] POST /api/projects - Creating project: '{}' by user: {} (id: {})", 
                request.getName(), currentUser != null ? currentUser.getEmail() : "NULL", 
                currentUser != null ? currentUser.getId() : "NULL");
        if (currentUser == null) {
            log.error("[ProjectController] POST /api/projects - currentUser is NULL! Authentication may have failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            ProjectResponse project = projectService.createProject(request, currentUser);
            log.info("[ProjectController] Project created successfully - id: {}, name: '{}'", project.getId(), project.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Project created successfully", project));
        } catch (Exception e) {
            log.error("[ProjectController] Failed to create project '{}' - {} - {}", 
                    request.getName(), e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProjects(@AuthenticationPrincipal User currentUser) {
        log.info("[ProjectController] GET /api/projects - Fetching all projects for user: {} (id: {})", 
                currentUser != null ? currentUser.getEmail() : "NULL",
                currentUser != null ? currentUser.getId() : "NULL");
        if (currentUser == null) {
            log.error("[ProjectController] GET /api/projects - currentUser is NULL! Authentication may have failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            List<ProjectResponse> projects = projectService.getAllProjects(currentUser);
            log.info("[ProjectController] Retrieved {} projects for user: {}", projects.size(), currentUser.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", projects));
        } catch (Exception e) {
            log.error("[ProjectController] Failed to fetch projects for user: {} - {} - {}", 
                    currentUser.getEmail(), e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("[ProjectController] GET /api/projects/{} - User: {}", id, 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[ProjectController] GET /api/projects/{} - currentUser is NULL!", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            ProjectResponse project = projectService.getProjectById(id, currentUser);
            log.info("[ProjectController] Project {} retrieved successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", project));
        } catch (Exception e) {
            log.error("[ProjectController] Failed to get project {} - {} - {}", 
                    id, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("[ProjectController] PUT /api/projects/{} - Updating to name: '{}' by user: {}", 
                id, request.getName(), currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[ProjectController] PUT /api/projects/{} - currentUser is NULL!", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            ProjectResponse project = projectService.updateProject(id, request, currentUser);
            log.info("[ProjectController] Project {} updated successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
        } catch (Exception e) {
            log.error("[ProjectController] Failed to update project {} - {} - {}", 
                    id, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("[ProjectController] DELETE /api/projects/{} - User: {}", id, 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[ProjectController] DELETE /api/projects/{} - currentUser is NULL!", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            projectService.deleteProject(id, currentUser);
            log.info("[ProjectController] Project {} deleted successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
        } catch (Exception e) {
            log.error("[ProjectController] Failed to delete project {} - {} - {}", 
                    id, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse> addMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("[ProjectController] POST /api/projects/{}/members/{} - Adding member, requestedBy: {}", 
                projectId, userId, currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[ProjectController] POST /api/projects/{}/members/{} - currentUser is NULL!", projectId, userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            ProjectResponse project = projectService.addMember(projectId, userId, currentUser);
            log.info("[ProjectController] Member {} added to project {} successfully", userId, projectId);
            return ResponseEntity.ok(ApiResponse.success("Member added successfully", project));
        } catch (Exception e) {
            log.error("[ProjectController] Failed to add member {} to project {} - {} - {}", 
                    userId, projectId, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("[ProjectController] DELETE /api/projects/{}/members/{} - Removing member, requestedBy: {}", 
                projectId, userId, currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[ProjectController] DELETE /api/projects/{}/members/{} - currentUser is NULL!", projectId, userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            ProjectResponse project = projectService.removeMember(projectId, userId, currentUser);
            log.info("[ProjectController] Member {} removed from project {} successfully", userId, projectId);
            return ResponseEntity.ok(ApiResponse.success("Member removed successfully", project));
        } catch (Exception e) {
            log.error("[ProjectController] Failed to remove member {} from project {} - {} - {}", 
                    userId, projectId, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
