package com.raslan.taskmanager.dto.Task;

import com.raslan.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTaskStatusDto {
    @NotBlank(message = "status is required")
    private TaskStatus status;
}
