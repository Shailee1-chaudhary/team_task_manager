package com.teamtask.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private UserSummary owner;
    private List<UserSummary> members;
    private int totalTasks;
    private int completedTasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
