package com.raslan.taskmanager.dto.Workspace;

import com.raslan.taskmanager.enums.WorkspaceStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateWorkspaceStatusDto {

    @NotBlank(message = "status is required")
    WorkspaceStatus status;
}
