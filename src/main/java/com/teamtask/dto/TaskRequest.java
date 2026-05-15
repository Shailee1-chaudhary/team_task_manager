package com.teamtask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 300)
    private String title;

    @Size(max = 2000)
    private String description;

    private String status; // TODO, IN_PROGRESS, DONE

    private String priority; // LOW, MEDIUM, HIGH

    private LocalDate dueDate;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assigneeId;
}
