package com.raslan.taskmanager.dto.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FireUserDto {
    @NotBlank(message = "user id is required")
    private Long userId;
}
