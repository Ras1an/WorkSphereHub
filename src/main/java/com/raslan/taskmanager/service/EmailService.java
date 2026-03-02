package com.raslan.taskmanager.service;


import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendEmail(String email, String token, String relativePath) {
        String link = "http://localhost:8080/auth/" + relativePath + "?token=" + token;

        // send mail
    }
}
