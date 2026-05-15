package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.TaskRequest;
import com.teamtask.dto.TaskResponse;
import com.teamtask.entity.User;
import com.teamtask.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User currentUser) {
        TaskResponse task = taskService.createTask(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse> getTasksByProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        List<TaskResponse> tasks = taskService.getTasksByProject(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<ApiResponse> getMyTasks(@AuthenticationPrincipal User currentUser) {
        List<TaskResponse> tasks = taskService.getMyTasks(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        TaskResponse task = taskService.getTaskById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User currentUser) {
        TaskResponse task = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            @AuthenticationPrincipal User currentUser) {
        String status = statusUpdate.get("status");
        TaskResponse task = taskService.updateTaskStatus(id, status, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }
}
