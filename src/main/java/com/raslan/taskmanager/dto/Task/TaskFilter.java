package com.raslan.taskmanager.dto.Task;

import com.raslan.taskmanager.enums.Priority;
import com.raslan.taskmanager.enums.SortBy;
import com.raslan.taskmanager.enums.SortingDirection;
import com.raslan.taskmanager.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TaskFilter {
    private int page =0;
    private int size = 5;
    private String title;
    private Long workspaceId;
    private TaskStatus status;
    private Priority priority;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SortBy sortBy;
    private SortingDirection sortingDir;
}
