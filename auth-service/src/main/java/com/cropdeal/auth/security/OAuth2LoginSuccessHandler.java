package com.cropdeal.auth.security;

import com.cropdeal.auth.entity.Role;
import com.cropdeal.auth.entity.User;
import com.cropdeal.auth.service.AuthService;
import com.cropdeal.auth.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(AuthService authService,
                                     RefreshTokenService refreshTokenService,
                                     JwtService jwtService,
                                     ObjectMapper objectMapper) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Facebook account did not provide an email address");
            return;
        }

        User user = authService.findOrCreateFederatedUser(email, name == null ? "Facebook User" : name,
                Role.FARMER);
        refreshTokenService.deleteByUserId(user.getId());
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "message", "Facebook login successful",
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "role", user.getRole().name()
        )));
    }
}
