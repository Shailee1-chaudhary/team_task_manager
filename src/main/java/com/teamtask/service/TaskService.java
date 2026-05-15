package com.teamtask.service;

import com.teamtask.dto.CommentResponse;
import com.teamtask.dto.TaskRequest;
import com.teamtask.dto.TaskResponse;
import com.teamtask.dto.UserSummary;
import com.teamtask.entity.*;
import com.teamtask.exception.AccessDeniedException;
import com.teamtask.exception.BadRequestException;
import com.teamtask.exception.ResourceNotFoundException;
import com.teamtask.repository.CommentRepository;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.TaskRepository;
import com.teamtask.repository.UserRepository;
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
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest request, User currentUser) {
        log.info("[TaskService] Creating task '{}' for projectId: {} by user: {} (id: {})", 
                request.getTitle(), request.getProjectId(), currentUser.getEmail(), currentUser.getId());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> {
                    log.error("[TaskService] Project not found with id: {}", request.getProjectId());
                    return new ResourceNotFoundException("Project not found with id: " + request.getProjectId());
                });

        log.debug("[TaskService] Project found: '{}' (id: {})", project.getName(), project.getId());
        validateProjectMembership(project, currentUser);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .createdBy(currentUser)
                .build();

        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
            log.debug("[TaskService] Task story points set to: {}", request.getStoryPoints());
        }

        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
                log.debug("[TaskService] Task status set to: {}", request.getStatus());
            } catch (IllegalArgumentException e) {
                log.error("[TaskService] Invalid status: '{}'", request.getStatus());
                throw new BadRequestException("Invalid status. Must be TODO, IN_PROGRESS, BLOCKED, CODE_REVIEW, QA_TESTING, QA_TESTING_FAILED, or DONE");
            }
        }

        if (request.getPriority() != null) {
            try {
                task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
                log.debug("[TaskService] Task priority set to: {}", request.getPriority());
            } catch (IllegalArgumentException e) {
                log.error("[TaskService] Invalid priority: '{}'", request.getPriority());
                throw new BadRequestException("Invalid priority. Must be LOW, MEDIUM, or HIGH");
            }
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
            log.debug("[TaskService] Task due date set to: {}", request.getDueDate());
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> {
                        log.error("[TaskService] Assignee not found with id: {}", request.getAssigneeId());
                        return new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId());
                    });
            task.setAssignee(assignee);
            log.debug("[TaskService] Task assigned to: {} (id: {})", assignee.getEmail(), assignee.getId());
        }

        task = taskRepository.save(task);
        log.info("[TaskService] Task created successfully - id: {}, title: '{}', status: {}, priority: {}", 
                task.getId(), task.getTitle(), task.getStatus(), task.getPriority());
        return mapToResponse(task);
    }

    public List<TaskResponse> getTasksByProject(Long projectId, User currentUser) {
        log.info("[TaskService] Getting tasks for projectId: {} by user: {}", projectId, currentUser.getEmail());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.error("[TaskService] Project not found with id: {}", projectId);
                    return new ResourceNotFoundException("Project not found with id: " + projectId);
                });

        validateProjectMembership(project, currentUser);

        List<Task> tasks = taskRepository.findByProject(project);
        log.info("[TaskService] Found {} tasks for projectId: {}", tasks.size(), projectId);
        return tasks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TaskResponse> getMyTasks(User currentUser) {
        log.info("[TaskService] Getting tasks assigned to user: {} (id: {})", 
                currentUser.getEmail(), currentUser.getId());
        List<Task> tasks = taskRepository.findByAssignee(currentUser);
        log.info("[TaskService] Found {} tasks for user: {}", tasks.size(), currentUser.getEmail());
        return tasks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id, User currentUser) {
        log.info("[TaskService] Getting task by id: {} for user: {}", id, currentUser.getEmail());
        Task task = findTaskById(id);
        validateTaskAccess(task, currentUser);
        return mapToResponse(task, true);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, User currentUser) {
        log.info("[TaskService] Updating task id: {} by user: {}", id, currentUser.getEmail());
        Task task = findTaskById(id);
        validateTaskAccess(task, currentUser);

        // Only admin or project owner can reassign tasks
        if (request.getAssigneeId() != null && !request.getAssigneeId().equals(
                task.getAssignee() != null ? task.getAssignee().getId() : null)) {
            log.debug("[TaskService] Reassigning task {} to userId: {}", id, request.getAssigneeId());
            validateAdminOrProjectOwner(task.getProject(), currentUser);
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> {
                        log.error("[TaskService] Assignee not found with id: {}", request.getAssigneeId());
                        return new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId());
                    });
            task.setAssignee(assignee);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
        }
        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.error("[TaskService] Invalid status on update: '{}'", request.getStatus());
                throw new BadRequestException("Invalid status. Must be TODO, IN_PROGRESS, BLOCKED, CODE_REVIEW, QA_TESTING, QA_TESTING_FAILED, or DONE");
            }
        }
        if (request.getPriority() != null) {
            try {
                task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.error("[TaskService] Invalid priority on update: '{}'", request.getPriority());
                throw new BadRequestException("Invalid priority. Must be LOW, MEDIUM, or HIGH");
            }
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        task = taskRepository.save(task);
        log.info("[TaskService] Task {} updated successfully - status: {}, priority: {}", 
                id, task.getStatus(), task.getPriority());
        return mapToResponse(task);
    }

    @Transactional
    public void deleteTask(Long id, User currentUser) {
        log.info("[TaskService] Deleting task id: {} by user: {}", id, currentUser.getEmail());
        Task task = findTaskById(id);
        validateAdminOrProjectOwner(task.getProject(), currentUser);
        taskRepository.delete(task);
        log.info("[TaskService] Task {} deleted successfully", id);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, String status, User currentUser) {
        log.info("[TaskService] Updating status for task id: {} to '{}' by user: {}", 
                id, status, currentUser.getEmail());
        Task task = findTaskById(id);
        validateTaskAccess(task, currentUser);

        try {
            task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.error("[TaskService] Invalid status: '{}'", status);
            throw new BadRequestException("Invalid status. Must be TODO, IN_PROGRESS, BLOCKED, CODE_REVIEW, QA_TESTING, QA_TESTING_FAILED, or DONE");
        }

        task = taskRepository.save(task);
        log.info("[TaskService] Task {} status updated to {} successfully", id, task.getStatus());
        return mapToResponse(task);
    }

    private Task findTaskById(Long id) {
        log.debug("[TaskService] Finding task by id: {}", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[TaskService] Task not found with id: {}", id);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });
    }

    private void validateProjectMembership(Project project, User user) {
        if (user.getRole() == Role.ADMIN) {
            log.debug("[TaskService] Admin access granted for project: {}", project.getId());
            return;
        }
        if (project.getOwner().getId().equals(user.getId())) {
            log.debug("[TaskService] Owner access granted for project: {}", project.getId());
            return;
        }
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
        if (!isMember) {
            log.warn("[TaskService] Project membership denied - user {} for project {}",
                    user.getEmail(), project.getId());
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    private void validateTaskAccess(Task task, User user) {
        if (user.getRole() == Role.ADMIN) {
            log.debug("[TaskService] Admin access granted for task: {}", task.getId());
            return;
        }
        // Allow access if user is the task assignee
        if (task.getAssignee() != null && task.getAssignee().getId().equals(user.getId())) {
            log.debug("[TaskService] Assignee access granted for task: {}", task.getId());
            return;
        }
        // Allow access if user is the task creator
        if (task.getCreatedBy() != null && task.getCreatedBy().getId().equals(user.getId())) {
            log.debug("[TaskService] Creator access granted for task: {}", task.getId());
            return;
        }
        Project project = task.getProject();
        if (project.getOwner().getId().equals(user.getId())) {
            log.debug("[TaskService] Owner access granted for task: {} (project: {})", task.getId(), project.getId());
            return;
        }
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
        if (!isMember) {
            log.warn("[TaskService] Task access denied - user {} for task {} (project: {})",
                    user.getEmail(), task.getId(), project.getId());
            throw new AccessDeniedException("You don't have access to this task");
        }
    }

    private void validateAdminOrProjectOwner(Project project, User user) {
        if (user.getRole() == Role.ADMIN) {
            log.debug("[TaskService] Admin privilege granted for project: {}", project.getId());
            return;
        }
        if (!project.getOwner().getId().equals(user.getId())) {
            log.warn("[TaskService] Admin/Owner action denied - user {} for project {}", 
                    user.getEmail(), project.getId());
            throw new AccessDeniedException("Only admin or project owner can perform this action");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        return mapToResponse(task, false);
    }

    private TaskResponse mapToResponse(Task task, boolean includeProgress) {
        long progressCount = commentRepository.countByTask(task);

        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .overdue(task.isOverdue())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .createdBy(mapUserToSummary(task.getCreatedBy()))
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .progressCount(progressCount);

        if (task.getAssignee() != null) {
            builder.assignee(mapUserToSummary(task.getAssignee()));
        }

        if (includeProgress) {
            List<Comment> notes = commentRepository.findByTaskOrderByCreatedAtDesc(task);
            builder.progressNotes(notes.stream().map(this::mapCommentToResponse).collect(Collectors.toList()));
        }

        return builder.build();
    }

    private CommentResponse mapCommentToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .taskId(comment.getTask().getId())
                .author(mapUserToSummary(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private UserSummary mapUserToSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
