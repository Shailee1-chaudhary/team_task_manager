package com.teamtask.service;

import com.teamtask.dto.ProjectRequest;
import com.teamtask.dto.ProjectResponse;
import com.teamtask.dto.UserSummary;
import com.teamtask.entity.Project;
import com.teamtask.entity.Role;
import com.teamtask.entity.TaskStatus;
import com.teamtask.entity.User;
import com.teamtask.exception.AccessDeniedException;
import com.teamtask.exception.ResourceNotFoundException;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.TaskRepository;
import com.teamtask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, User currentUser) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();

        project.getMembers().add(currentUser);
        project = projectRepository.save(project);

        return mapToResponse(project);
    }

    public List<ProjectResponse> getAllProjects(User currentUser) {
        List<Project> projects;

        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByOwnerOrMember(currentUser);
        }

        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id, User currentUser) {
        Project project = findProjectById(id);
        validateProjectAccess(project, currentUser);
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, User currentUser) {
        Project project = findProjectById(id);
        validateProjectOwnerOrAdmin(project, currentUser);

        project.setName(request.getName());
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(Long id, User currentUser) {
        Project project = findProjectById(id);
        validateProjectOwnerOrAdmin(project, currentUser);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, Long userId, User currentUser) {
        Project project = findProjectById(projectId);
        validateProjectOwnerOrAdmin(project, currentUser);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        project.getMembers().add(user);
        project = projectRepository.save(project);

        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long userId, User currentUser) {
        Project project = findProjectById(projectId);
        validateProjectOwnerOrAdmin(project, currentUser);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (project.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot remove the project owner");
        }

        project.getMembers().remove(user);
        project = projectRepository.save(project);

        return mapToResponse(project);
    }

    public Project findProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private void validateProjectAccess(Project project, User user) {
        if (user.getRole() == Role.ADMIN) return;
        if (!project.getOwner().getId().equals(user.getId()) &&
                !project.getMembers().contains(user)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
    }

    private void validateProjectOwnerOrAdmin(Project project, User user) {
        if (user.getRole() == Role.ADMIN) return;
        if (!project.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the project owner or admin can perform this action");
        }
    }

    private ProjectResponse mapToResponse(Project project) {
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
