package com.raslan.taskmanager.dto.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestResetPassword {
    @NotBlank(message = "email is required")
    private String email;
}
