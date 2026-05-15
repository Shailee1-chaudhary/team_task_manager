package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.TaskRequest;
import com.teamtask.dto.TaskResponse;
import com.teamtask.entity.User;
import com.teamtask.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("[TaskController] POST /api/tasks - Creating task: '{}' for projectId: {} by user: {}", 
                request.getTitle(), request.getProjectId(), 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[TaskController] POST /api/tasks - currentUser is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            TaskResponse task = taskService.createTask(request, currentUser);
            log.info("[TaskController] Task created - id: {}, title: '{}', projectId: {}", 
                    task.getId(), task.getTitle(), task.getProjectId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Task created successfully", task));
        } catch (Exception e) {
            log.error("[TaskController] Failed to create task '{}' - {} - {}", 
                    request.getTitle(), e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse> getTasksByProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("[TaskController] GET /api/tasks/project/{} - User: {}", projectId, 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[TaskController] GET /api/tasks/project/{} - currentUser is NULL!", projectId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            List<TaskResponse> tasks = taskService.getTasksByProject(projectId, currentUser);
            log.info("[TaskController] Retrieved {} tasks for projectId: {}", tasks.size(), projectId);
            return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
        } catch (Exception e) {
            log.error("[TaskController] Failed to get tasks for project {} - {} - {}", 
                    projectId, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<ApiResponse> getMyTasks(@AuthenticationPrincipal User currentUser) {
        log.info("[TaskController] GET /api/tasks/my-tasks - User: {}", 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[TaskController] GET /api/tasks/my-tasks - currentUser is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            List<TaskResponse> tasks = taskService.getMyTasks(currentUser);
            log.info("[TaskController] Retrieved {} tasks for user: {}", tasks.size(), currentUser.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
        } catch (Exception e) {
            log.error("[TaskController] Failed to get tasks for user {} - {} - {}", 
                    currentUser.getEmail(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("[TaskController] GET /api/tasks/{} - User: {}", id, 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[TaskController] GET /api/tasks/{} - currentUser is NULL!", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            TaskResponse task = taskService.getTaskById(id, currentUser);
            log.info("[TaskController] Task {} retrieved successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
        } catch (Exception e) {
            log.error("[TaskController] Failed to get task {} - {} - {}", 
                    id, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("[TaskController] PUT /api/tasks/{} - Updating task by user: {}", id, 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[TaskController] PUT /api/tasks/{} - currentUser is NULL!", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            TaskResponse task = taskService.updateTask(id, request, currentUser);
            log.info("[TaskController] Task {} updated successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
        } catch (Exception e) {
            log.error("[TaskController] Failed to update task {} - {} - {}", 
                    id, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            @AuthenticationPrincipal User currentUser) {
        String status = statusUpdate.get("status");
        log.info("[TaskController] PATCH /api/tasks/{}/status - New status: '{}' by user: {}", 
                id, status, currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[TaskController] PATCH /api/tasks/{}/status - currentUser is NULL!", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            TaskResponse task = taskService.updateTaskStatus(id, status, currentUser);
            log.info("[TaskController] Task {} status updated to '{}' successfully", id, status);
            return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", task));
        } catch (Exception e) {
            log.error("[TaskController] Failed to update status for task {} - {} - {}", 
                    id, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("[TaskController] DELETE /api/tasks/{} - User: {}", id, 
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            log.error("[TaskController] DELETE /api/tasks/{} - currentUser is NULL!", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please log in again."));
        }
        try {
            taskService.deleteTask(id, currentUser);
            log.info("[TaskController] Task {} deleted successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
        } catch (Exception e) {
            log.error("[TaskController] Failed to delete task {} - {} - {}", 
                    id, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
