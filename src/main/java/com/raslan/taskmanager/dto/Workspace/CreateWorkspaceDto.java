package com.raslan.taskmanager.dto.Workspace;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateWorkspaceDto {
    @NotBlank(message = "workspace name required")
    private String name;

    private String description;
    private LocalDateTime deadline;
}
