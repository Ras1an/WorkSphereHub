package com.raslan.taskmanager.dto.Task;

import com.raslan.taskmanager.enums.Priority;
import com.raslan.taskmanager.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTaskDto {
    private String title;

    private String description;

    private TaskStatus status;

    private Priority priority;

    private LocalDateTime deadline;

}
