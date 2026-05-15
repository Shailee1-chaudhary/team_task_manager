package com.teamtask.service;

import com.teamtask.dto.*;
import com.teamtask.entity.*;
import com.teamtask.repository.ProjectRepository;
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
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardResponse getDashboard(User currentUser) {
        log.info("[DashboardService] Building dashboard for user: {} (id: {}, role: {})", 
                currentUser.getEmail(), currentUser.getId(), currentUser.getRole());

        List<Project> projects;
        List<Task> allTasks;

        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
            allTasks = taskRepository.findAll();
            log.debug("[DashboardService] Admin user - fetching all data");
        } else {
            projects = projectRepository.findByOwnerOrMember(currentUser);
            allTasks = taskRepository.findByAssigneeOrCreatedBy(currentUser);
            log.debug("[DashboardService] Member user - fetching user-specific data");
        }

        long totalTasks = allTasks.size();
        long todoTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgressTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long blockedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();
        long codeReviewTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.CODE_REVIEW).count();
        long qaTestingTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.QA_TESTING).count();
        long qaTestingFailedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.QA_TESTING_FAILED).count();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        List<Task> overdueTasks = allTasks.stream()
                .filter(Task::isOverdue)
                .collect(Collectors.toList());

        List<TaskResponse> recentTasks = allTasks.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .map(this::mapTaskToResponse)
                .collect(Collectors.toList());

        List<TaskResponse> overdueTaskResponses = overdueTasks.stream()
                .map(this::mapTaskToResponse)
                .collect(Collectors.toList());

        List<ProjectResponse> projectResponses = projects.stream()
                .map(this::mapProjectToResponse)
                .collect(Collectors.toList());

        log.info("[DashboardService] Dashboard built - projects: {}, totalTasks: {}, todo: {}, inProgress: {}, blocked: {}, codeReview: {}, qaTesting: {}, qaTestingFailed: {}, done: {}, overdue: {}",
                projects.size(), totalTasks, todoTasks, inProgressTasks, blockedTasks, codeReviewTasks, qaTestingTasks, qaTestingFailedTasks, completedTasks, overdueTasks.size());

        return DashboardResponse.builder()
                .totalProjects(projects.size())
                .totalTasks(totalTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .blockedTasks(blockedTasks)
                .codeReviewTasks(codeReviewTasks)
                .qaTestingTasks(qaTestingTasks)
                .qaTestingFailedTasks(qaTestingFailedTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks.size())
                .recentTasks(recentTasks)
                .overDueTaskList(overdueTaskResponses)
                .projects(projectResponses)
                .build();
    }

    private TaskResponse mapTaskToResponse(Task task) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .taskNumber("TSK_" + task.getId())
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

    private ProjectResponse mapProjectToResponse(Project project) {
        List<UserSummary> memberSummaries = project.getMembers().stream()
                .map(this::mapUserToSummary)
                .collect(Collectors.toList());

        int totalTasks = project.getTasks().size();
        int completedTasks = (int) project.getTasks().stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .owner(mapUserToSummary(project.getOwner()))
                .members(memberSummaries)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
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
