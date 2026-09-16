package com.cropdeal.auth.controller;

import com.cropdeal.auth.dto.*;
import com.cropdeal.auth.entity.User;
import com.cropdeal.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/farmer")
    public ResponseEntity<AuthResponse> registerFarmer(@Valid @RequestBody FarmerRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(authService.registerFarmer(request)));
    }

    @PostMapping("/register/dealer")
    public ResponseEntity<AuthResponse> registerDealer(@Valid @RequestBody DealerRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(authService.registerDealer(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(Authentication authentication) {
        return ResponseEntity.ok(Map.of("valid", true, "user", authentication.getName()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String message =
                authService.createPasswordResetToken(request.getEmail());

        return ResponseEntity.ok(
                Map.of("message", message)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @PutMapping("/admin/users/{id}/status")
    public ResponseEntity<AuthResponse> setActiveStatus(
            @PathVariable Long id,
            @Valid @RequestBody ActiveStatusRequest request) {

        return ResponseEntity.ok(
                toResponse(authService.setActiveStatus(id, request.getActive()))
        );
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<AuthResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(authService.getUser(id)));
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), user.isActive());
    }
}
