package com.teamtask.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalProjects;
    private long totalTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long overdueTasks;
    private List<TaskResponse> recentTasks;
    private List<TaskResponse> overDueTaskList;
    private List<ProjectResponse> projects;
}
