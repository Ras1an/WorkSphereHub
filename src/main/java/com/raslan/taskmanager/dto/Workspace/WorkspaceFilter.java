package com.raslan.taskmanager.dto.Workspace;

import com.raslan.taskmanager.enums.SortBy;
import com.raslan.taskmanager.enums.SortingDirection;
import com.raslan.taskmanager.enums.WorkspaceStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceFilter {
    private int page = 0;
    private int size = 6;
    private String name;
    private WorkspaceStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SortBy sortBy;
    private SortingDirection sortingDir;
}
