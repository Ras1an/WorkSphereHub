package com.raslan.taskmanager.dto.Auth;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDto {

    @NotBlank(message = "token is required")
    private String token;

    @NotBlank(message = "password is required")
    private String password;
}
