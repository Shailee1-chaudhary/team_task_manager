package com.teamtask.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private boolean overdue;
    private Long projectId;
    private String projectName;
    private UserSummary assignee;
    private UserSummary createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
