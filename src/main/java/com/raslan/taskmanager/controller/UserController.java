package com.raslan.taskmanager.controller;

import com.raslan.taskmanager.dto.User.UserInformation;
import com.raslan.taskmanager.service.UserService;
import com.raslan.taskmanager.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("user")
    public ResponseEntity<UserInformation> getUserInformation(@AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(userService.getUserInformation(user.getId()));
    }


}
