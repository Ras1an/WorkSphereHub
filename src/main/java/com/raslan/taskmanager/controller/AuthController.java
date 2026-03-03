package com.raslan.taskmanager.controller;


import com.raslan.taskmanager.dto.Auth.*;
import com.raslan.taskmanager.model.Token;
import com.raslan.taskmanager.model.User;
import com.raslan.taskmanager.service.AuthService;
import com.raslan.taskmanager.service.JwtService;
import com.raslan.taskmanager.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    Authentication auth =  authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password));
    UserPrincipal user = (UserPrincipal) auth.getPrincipal();
    String accessToken = jwtService.generateToken(user.getEmail(), user.getId());
    String refreshToken = authService.createRefreshToken(user.getId());

    return new ResponseEntity<>(new AuthResponse(accessToken, refreshToken), HttpStatus.OK);
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

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request){
        Token oldRefreshToken = authService.validateRefreshToken(request.refreshToken());

        User user = oldRefreshToken.getUser();

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getId());

        String newRefreshToken = authService.createRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal UserPrincipal user){
        authService.deleteRefreshTokens(user.getId());
        return ResponseEntity.ok("Logout Successfully");

    }


}
