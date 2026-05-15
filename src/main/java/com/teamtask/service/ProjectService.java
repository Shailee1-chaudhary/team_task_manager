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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, User currentUser) {
        log.info("[ProjectService] Creating project '{}' for user: {} (id: {})", 
                request.getName(), currentUser.getEmail(), currentUser.getId());
        
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();

        project.getMembers().add(currentUser);
        project = projectRepository.save(project);

        log.info("[ProjectService] Project created - id: {}, name: '{}', ownerId: {}", 
                project.getId(), project.getName(), currentUser.getId());
        return mapToResponse(project);
    }

    public List<ProjectResponse> getAllProjects(User currentUser) {
        log.info("[ProjectService] Fetching all projects for user: {} (id: {}, role: {})", 
                currentUser.getEmail(), currentUser.getId(), currentUser.getRole());
        
        List<Project> projects;

        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
            log.debug("[ProjectService] Admin user - fetching all projects");
        } else {
            projects = projectRepository.findByOwnerOrMember(currentUser);
            log.debug("[ProjectService] Member user - fetching owned/member projects");
        }

        log.info("[ProjectService] Found {} projects for user: {}", projects.size(), currentUser.getEmail());
        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id, User currentUser) {
        log.info("[ProjectService] Getting project by id: {} for user: {}", id, currentUser.getEmail());
        Project project = findProjectById(id);
        validateProjectAccess(project, currentUser);
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, User currentUser) {
        log.info("[ProjectService] Updating project id: {} to name: '{}' by user: {}", 
                id, request.getName(), currentUser.getEmail());
        
        Project project = findProjectById(id);
        validateProjectOwnerOrAdmin(project, currentUser);

        project.setName(request.getName());
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        project = projectRepository.save(project);
        log.info("[ProjectService] Project {} updated successfully", id);
        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(Long id, User currentUser) {
        log.info("[ProjectService] Deleting project id: {} by user: {}", id, currentUser.getEmail());
        Project project = findProjectById(id);
        validateProjectOwnerOrAdmin(project, currentUser);
        projectRepository.delete(project);
        log.info("[ProjectService] Project {} deleted successfully", id);
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, Long userId, User currentUser) {
        log.info("[ProjectService] Adding member userId: {} to project: {} by user: {}", 
                userId, projectId, currentUser.getEmail());
        
        Project project = findProjectById(projectId);
        validateProjectOwnerOrAdmin(project, currentUser);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[ProjectService] User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        project.getMembers().add(user);
        project = projectRepository.save(project);

        log.info("[ProjectService] Member {} added to project {} successfully", userId, projectId);
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long userId, User currentUser) {
        log.info("[ProjectService] Removing member userId: {} from project: {} by user: {}", 
                userId, projectId, currentUser.getEmail());
        
        Project project = findProjectById(projectId);
        validateProjectOwnerOrAdmin(project, currentUser);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[ProjectService] User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        if (project.getOwner().getId().equals(userId)) {
            log.warn("[ProjectService] Attempted to remove project owner (userId: {}) from project: {}", 
                    userId, projectId);
            throw new AccessDeniedException("Cannot remove the project owner");
        }

        project.getMembers().remove(user);
        project = projectRepository.save(project);

        log.info("[ProjectService] Member {} removed from project {} successfully", userId, projectId);
        return mapToResponse(project);
    }

    public Project findProjectById(Long id) {
        log.debug("[ProjectService] Finding project by id: {}", id);
        return projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[ProjectService] Project not found with id: {}", id);
                    return new ResourceNotFoundException("Project not found with id: " + id);
                });
    }

    private void validateProjectAccess(Project project, User user) {
        if (user.getRole() == Role.ADMIN) {
            log.debug("[ProjectService] Admin access granted for project: {}", project.getId());
            return;
        }
        if (!project.getOwner().getId().equals(user.getId()) &&
                !project.getMembers().contains(user)) {
            log.warn("[ProjectService] Access denied - user {} tried to access project {}", 
                    user.getEmail(), project.getId());
            throw new AccessDeniedException("You don't have access to this project");
        }
    }

    private void validateProjectOwnerOrAdmin(Project project, User user) {
        if (user.getRole() == Role.ADMIN) {
            log.debug("[ProjectService] Admin privilege granted for project: {}", project.getId());
            return;
        }
        if (!project.getOwner().getId().equals(user.getId())) {
            log.warn("[ProjectService] Owner/Admin action denied - user {} on project {} (owner: {})", 
                    user.getEmail(), project.getId(), project.getOwner().getEmail());
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
