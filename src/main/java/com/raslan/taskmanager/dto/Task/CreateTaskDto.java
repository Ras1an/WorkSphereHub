package com.raslan.taskmanager.dto.Task;

import com.raslan.taskmanager.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskDto {

    @NotBlank(message = "Task title required")
    private String title;

    private String description;

    @NotBlank(message = "Task priority required")
    private Priority priority;

    private LocalDateTime deadline;

    @NotBlank(message = "workspace id required")
    private Long workspaceId;

    @NotBlank(message = "user id is required to assign the task")
    private Long assignedToId;
}
