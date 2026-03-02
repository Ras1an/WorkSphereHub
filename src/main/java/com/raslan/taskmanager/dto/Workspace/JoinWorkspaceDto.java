package com.raslan.taskmanager.dto.Workspace;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinWorkspaceDto {
    @NotBlank(message = "workspace code required")
    String code;
}
