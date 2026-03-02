package com.raslan.taskmanager.service;

import com.raslan.taskmanager.dto.User.UserInformation;
import com.raslan.taskmanager.model.User;
import com.raslan.taskmanager.repository.UserRepository;
import com.raslan.taskmanager.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private UserRepository  userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public UserInformation getUserInformation(Long userId){
        User user = userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found"));

        return UserInformation.builder()
                .name(user.getName()).email(user.getEmail())
                .build();
    }
}
