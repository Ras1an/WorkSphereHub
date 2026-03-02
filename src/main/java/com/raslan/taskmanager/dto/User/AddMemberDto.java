package com.raslan.taskmanager.dto.User;

import com.raslan.taskmanager.enums.WorkspaceUserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberDto {
    @NotBlank(message = "user email required")
    String invitedUserEmail;
    @NotBlank(message = "user role required")
    WorkspaceUserRole role;
}
