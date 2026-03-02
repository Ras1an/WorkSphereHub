package com.raslan.taskmanager.service;

import com.raslan.taskmanager.exception.BadRequestException;
import com.raslan.taskmanager.exception.ConflictException;
import com.raslan.taskmanager.dto.Auth.RegisterRequest;
import com.raslan.taskmanager.dto.Auth.RequestResetPassword;
import com.raslan.taskmanager.dto.Auth.ResetPasswordDto;
import com.raslan.taskmanager.model.Token;
import com.raslan.taskmanager.model.User;
import com.raslan.taskmanager.repository.TokenRepository;
import com.raslan.taskmanager.repository.UserRepository;
import com.raslan.taskmanager.enums.Role;
import com.raslan.taskmanager.enums.TokenType;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepo;
    private final EmailService emailService;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder,  TokenRepository tokenRepo,  EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
    }


    public void register(RegisterRequest request){
        if(userRepo.findByEmail(request.getEmail()).isPresent())
            throw new ConflictException("Email Already Exists");

        String pass = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName(), request.getEmail(),
                pass, Role.USER);

        userRepo.save(user);

        generateToken(user, TokenType.VERIFY_EMAIL, Duration.ofHours(24));
    }

    @Transactional
    public void verifyEmail(String tokenStr){
        Token token = tokenRepo.findByToken(tokenStr).orElseThrow(() -> new BadRequestException("Invalid Token"));

        if(token.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Token expired");


        User user = token.getUser();
        user.setVerified(true);
        tokenRepo.delete(token);
    }

    public void requestResetPassword(RequestResetPassword resetPasswordDto) {
        Optional<User> optionalUser = userRepo.findByEmail(resetPasswordDto.getEmail());
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            if(user.isVerified()) {
                String token = generateToken(user, TokenType.RESET_PASSWORD, Duration.ofMinutes(30));
                emailService.sendEmail(user.getEmail(), token, "confirm");
            }
        }
    }

    @Transactional
    public void confirmResetPassword(ResetPasswordDto resetPasswordDto) {
        Token token = tokenRepo.findByToken(resetPasswordDto.getToken()).orElseThrow(() -> new BadRequestException("Invalid Token"));

        if(token.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Token expired");


        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
        tokenRepo.delete(token);
    }


    private String generateToken(User user, TokenType tokenType, Duration duration){
        tokenRepo.deleteByUserAndTokenType(user, tokenType);

        String tokenStr = UUID.randomUUID().toString();
        Token token =  new Token(tokenStr, TokenType.VERIFY_EMAIL, LocalDateTime.now().plus(duration), user);
        tokenRepo.save(token);

        return tokenStr;
    }

    public void resendVerification(String email) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            if(!user.isVerified()){
                String token = generateToken(user, TokenType.VERIFY_EMAIL, Duration.ofHours(24));
                emailService.sendEmail(user.getEmail(), token, "verify");
            }
        }

    }
}
