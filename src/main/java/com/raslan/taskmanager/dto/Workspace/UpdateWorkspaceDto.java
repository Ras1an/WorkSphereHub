package com.raslan.taskmanager.dto.Workspace;

import com.raslan.taskmanager.enums.WorkspaceStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateWorkspaceDto {
    private String name;
    private String description;
    private LocalDateTime deadline;
    private WorkspaceStatus workspaceStatus;

}
