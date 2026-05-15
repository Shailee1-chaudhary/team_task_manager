package com.teamtask.service;

import com.teamtask.dto.CommentRequest;
import com.teamtask.dto.CommentResponse;
import com.teamtask.dto.UserSummary;
import com.teamtask.entity.*;
import com.teamtask.exception.AccessDeniedException;
import com.teamtask.exception.ResourceNotFoundException;
import com.teamtask.repository.CommentRepository;
import com.teamtask.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public CommentResponse addProgress(Long taskId, CommentRequest request, User currentUser) {
        log.info("[CommentService] Adding progress note to task {} by user {}", taskId, currentUser.getEmail());

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        validateTaskAccess(task, currentUser);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .author(currentUser)
                .build();

        comment = commentRepository.save(comment);
        log.info("[CommentService] Progress note {} added to task {}", comment.getId(), taskId);
        return mapToResponse(comment);
    }

    public List<CommentResponse> getProgressByTask(Long taskId, User currentUser) {
        log.info("[CommentService] Getting progress notes for task {} by user {}", taskId, currentUser.getEmail());

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        validateTaskAccess(task, currentUser);

        List<Comment> comments = commentRepository.findByTaskOrderByCreatedAtDesc(task);
        log.info("[CommentService] Found {} progress notes for task {}", comments.size(), taskId);
        return comments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteProgress(Long commentId, User currentUser) {
        log.info("[CommentService] Deleting progress note {} by user {}", commentId, currentUser.getEmail());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress note not found with id: " + commentId));

        // Only the author or admin can delete
        if (!comment.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You can only delete your own progress notes");
        }

        commentRepository.delete(comment);
        log.info("[CommentService] Progress note {} deleted", commentId);
    }

    private void validateTaskAccess(Task task, User user) {
        if (user.getRole() == Role.ADMIN) return;
        // Allow access if user is the task assignee
        if (task.getAssignee() != null && task.getAssignee().getId().equals(user.getId())) return;
        // Allow access if user is the task creator
        if (task.getCreatedBy() != null && task.getCreatedBy().getId().equals(user.getId())) return;
        Project project = task.getProject();
        if (!project.getOwner().getId().equals(user.getId()) &&
                !project.getMembers().contains(user)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .taskId(comment.getTask().getId())
                .author(UserSummary.builder()
                        .id(comment.getAuthor().getId())
                        .name(comment.getAuthor().getName())
                        .email(comment.getAuthor().getEmail())
                        .role(comment.getAuthor().getRole().name())
                        .build())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
