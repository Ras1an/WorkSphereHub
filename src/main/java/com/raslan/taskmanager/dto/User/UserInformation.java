package com.raslan.taskmanager.dto.User;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInformation {
    private String name;
    private String email;
}
