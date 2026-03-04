package com.raslan.taskmanager.dto.User;

import com.raslan.taskmanager.enums.WorkspaceUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberDto {
    @NotBlank(message = "user email required")
    String invitedUserEmail;
    @NotNull(message = "user role required")
    WorkspaceUserRole role;
}
