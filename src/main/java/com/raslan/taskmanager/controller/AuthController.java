package com.raslan.taskmanager.controller;


import com.raslan.taskmanager.dto.Auth.*;
import com.raslan.taskmanager.service.AuthService;
import com.raslan.taskmanager.service.JwtService;
import com.raslan.taskmanager.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authManager = authenticationManager;
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication auth;
        try {
        auth =  authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password));
    }
    catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Account not verified"));
        }
    catch (BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("email or password incorrect"));
    }
    UserPrincipal user = (UserPrincipal) auth.getPrincipal();
    String token = jwtService.generateToken(user.getEmail(), user.getId());
    return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){
        authService.register(request);
        return ResponseEntity.ok("Register Successfully");
    }
    @PostMapping("resend")
    public ResponseEntity<String> resend(@RequestBody ResendVerificationRequest request){
        authService.resendVerification(request.getEmail());
        return ResponseEntity.ok("If the email exists, a verification link has been sent");
    }

    @GetMapping("verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token){
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email Verified Successfully");
    }

    @PostMapping("reset")
    public ResponseEntity<String> requestResetPassword(@RequestBody RequestResetPassword resetPasswordDto){
        authService.requestResetPassword(resetPasswordDto);
        return ResponseEntity.ok("If the email exists, a password reset link has been sent");
    }

    @PostMapping("confirm")
    public ResponseEntity<String> confirmResetPassword(@RequestBody ResetPasswordDto resetPasswordDto){
        authService.confirmResetPassword(resetPasswordDto);
        return ResponseEntity.ok("Password Reset successfully");
    }


}
