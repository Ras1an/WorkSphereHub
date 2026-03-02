package com.raslan.taskmanager.dto.Auth;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "email required")
    public String email;

    @NotBlank(message = "password required")
    public String password;
}
