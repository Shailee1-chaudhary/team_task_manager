package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import com.teamtask.dto.CommentRequest;
import com.teamtask.dto.CommentResponse;
import com.teamtask.entity.User;
import com.teamtask.service.CommentService;
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
@RequestMapping("/api/tasks/{taskId}/progress")
@RequiredArgsConstructor
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse> addProgress(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("[CommentController] POST /api/tasks/{}/progress by user: {}", taskId,
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        CommentResponse response = commentService.addProgress(taskId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Progress note added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getProgress(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        log.info("[CommentController] GET /api/tasks/{}/progress by user: {}", taskId,
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        List<CommentResponse> notes = commentService.getProgressByTask(taskId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Progress notes retrieved successfully", notes));
    }

    @DeleteMapping("/{progressId}")
    public ResponseEntity<ApiResponse> deleteProgress(
            @PathVariable Long taskId,
            @PathVariable Long progressId,
            @AuthenticationPrincipal User currentUser) {
        log.info("[CommentController] DELETE /api/tasks/{}/progress/{} by user: {}", taskId, progressId,
                currentUser != null ? currentUser.getEmail() : "NULL");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        commentService.deleteProgress(progressId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Progress note deleted successfully"));
    }
}
