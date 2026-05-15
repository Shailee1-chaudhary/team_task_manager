package com.teamtask.repository;

import com.teamtask.entity.Project;
import com.teamtask.entity.Task;
import com.teamtask.entity.TaskStatus;
import com.teamtask.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProject(Project project);

    List<Task> findByAssignee(User assignee);

    List<Task> findByProjectAndStatus(Project project, TaskStatus status);

    List<Task> findByAssigneeAndStatus(User assignee, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignee = :user AND t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasksByUser(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasksByProject(@Param("project") Project project, @Param("today") LocalDate today);

    long countByAssigneeAndStatus(User assignee, TaskStatus status);

    long countByProjectAndStatus(Project project, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignee = :user OR t.createdBy = :user")
    List<Task> findByAssigneeOrCreatedBy(@Param("user") User user);
}
