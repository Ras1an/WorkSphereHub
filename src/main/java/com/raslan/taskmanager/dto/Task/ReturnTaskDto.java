package com.raslan.taskmanager.dto.Task;


import com.raslan.taskmanager.enums.Priority;
import com.raslan.taskmanager.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReturnTaskDto {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private TaskStatus status;
    private LocalDateTime deadline;
}
