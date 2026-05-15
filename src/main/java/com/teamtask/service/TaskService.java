package com.teamtask.service;

import com.teamtask.dto.TaskRequest;
import com.teamtask.dto.TaskResponse;
import com.teamtask.dto.UserSummary;
import com.teamtask.entity.*;
import com.teamtask.exception.AccessDeniedException;
import com.teamtask.exception.BadRequestException;
import com.teamtask.exception.ResourceNotFoundException;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.TaskRepository;
import com.teamtask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest request, User currentUser) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        validateProjectMembership(project, currentUser);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .createdBy(currentUser)
                .build();

        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status. Must be TODO, IN_PROGRESS, or DONE");
            }
        }

        if (request.getPriority() != null) {
            try {
                task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid priority. Must be LOW, MEDIUM, or HIGH");
            }
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    public List<TaskResponse> getTasksByProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        validateProjectMembership(project, currentUser);

        return taskRepository.findByProject(project).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getMyTasks(User currentUser) {
        return taskRepository.findByAssignee(currentUser).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id, User currentUser) {
        Task task = findTaskById(id);
        validateTaskAccess(task, currentUser);
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, User currentUser) {
        Task task = findTaskById(id);
        validateTaskAccess(task, currentUser);

        // Only admin or project owner can reassign tasks
        if (request.getAssigneeId() != null && !request.getAssigneeId().equals(
                task.getAssignee() != null ? task.getAssignee().getId() : null)) {
            validateAdminOrProjectOwner(task.getProject(), currentUser);
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status. Must be TODO, IN_PROGRESS, or DONE");
            }
        }
        if (request.getPriority() != null) {
            try {
                task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid priority. Must be LOW, MEDIUM, or HIGH");
            }
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Transactional
    public void deleteTask(Long id, User currentUser) {
        Task task = findTaskById(id);
        validateAdminOrProjectOwner(task.getProject(), currentUser);
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, String status, User currentUser) {
        Task task = findTaskById(id);
        validateTaskAccess(task, currentUser);

        try {
            task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be TODO, IN_PROGRESS, or DONE");
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private void validateProjectMembership(Project project, User user) {
        if (user.getRole() == Role.ADMIN) return;
        if (!project.getOwner().getId().equals(user.getId()) &&
                !project.getMembers().contains(user)) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    private void validateTaskAccess(Task task, User user) {
        if (user.getRole() == Role.ADMIN) return;
        Project project = task.getProject();
        if (!project.getOwner().getId().equals(user.getId()) &&
                !project.getMembers().contains(user)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
    }

    private void validateAdminOrProjectOwner(Project project, User user) {
        if (user.getRole() == Role.ADMIN) return;
        if (!project.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only admin or project owner can perform this action");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .overdue(task.isOverdue())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .createdBy(mapUserToSummary(task.getCreatedBy()))
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt());

        if (task.getAssignee() != null) {
            builder.assignee(mapUserToSummary(task.getAssignee()));
        }

        return builder.build();
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
