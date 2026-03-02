package com.raslan.taskmanager.security;

import com.raslan.taskmanager.enums.Role;
import com.raslan.taskmanager.model.User;
import com.raslan.taskmanager.repository.UserRepository;
import com.raslan.taskmanager.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository  userRepo;
    private final JwtService jwtService;

    public OAuth2LoginSuccessHandler(UserRepository userRepo,  JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email").toString();
        String name = oAuth2User.getAttribute("name").toString();

        User user = userRepo.findByEmail(email).orElseGet(() -> userRepo.save(new User(name, email, "", Role.USER, true)));
        String token = jwtService.generateToken(user.getEmail(), user.getId());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"token\": \"" + token + "\"}"
        );

    }
}
