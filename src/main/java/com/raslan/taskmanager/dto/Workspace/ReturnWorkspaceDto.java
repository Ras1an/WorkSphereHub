package com.raslan.taskmanager.dto.Workspace;

import com.raslan.taskmanager.enums.WorkspaceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnWorkspaceDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private WorkspaceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
}
