package com.cropdeal.auth.controller;

import com.cropdeal.auth.dto.AuthResponse;
import com.cropdeal.auth.dto.FacebookDemoAccountResponse;
import com.cropdeal.auth.dto.LoginRequest;
import com.cropdeal.auth.dto.MockFacebookLoginRequest;
import com.cropdeal.auth.dto.RegisterRequest;
import com.cropdeal.auth.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		AuthResponse response = authService.register(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("/register/request-otp")
	public ResponseEntity<com.cropdeal.auth.dto.MessageResponse> requestRegistrationOtp(
			@Valid @RequestBody com.cropdeal.auth.dto.RequestOtpRequest request) {
		return ResponseEntity.ok(authService.requestRegistrationOtp(request));
	}

	@PostMapping("/register/verify-otp")
	public ResponseEntity<com.cropdeal.auth.dto.MessageResponse> verifyRegistrationOtp(
			@Valid @RequestBody com.cropdeal.auth.dto.VerifyOtpRequest request) {
		return ResponseEntity.ok(authService.verifyRegistrationOtp(request));
	}

	@GetMapping("/facebook/accounts")
	public ResponseEntity<List<FacebookDemoAccountResponse>> facebookAccounts() {
		return ResponseEntity.ok(authService.getFacebookDemoAccounts());
	}

	@PostMapping("/facebook/mock")
	public ResponseEntity<AuthResponse> mockFacebook(@Valid @RequestBody MockFacebookLoginRequest request) {
		return ResponseEntity.ok(authService.mockFacebookLogin(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<com.cropdeal.auth.dto.PasswordResetResponse> forgotPassword(
			@Valid @RequestBody com.cropdeal.auth.dto.ForgotPasswordRequest request) {
		return ResponseEntity.ok(authService.forgotPassword(request));
	}

	@PostMapping("/forgot-password/request-otp")
	public ResponseEntity<com.cropdeal.auth.dto.MessageResponse> requestOtp(
			@Valid @RequestBody com.cropdeal.auth.dto.RequestOtpRequest request) {
		return ResponseEntity.ok(authService.requestOtp(request));
	}

	@PostMapping("/forgot-password/verify-otp")
	public ResponseEntity<com.cropdeal.auth.dto.MessageResponse> verifyOtp(
			@Valid @RequestBody com.cropdeal.auth.dto.VerifyOtpRequest request) {
		return ResponseEntity.ok(authService.verifyOtp(request));
	}

	@PostMapping("/forgot-password/reset")
	public ResponseEntity<com.cropdeal.auth.dto.MessageResponse> resetPasswordWithOtp(
			@Valid @RequestBody com.cropdeal.auth.dto.ResetWithOtpRequest request) {
		return ResponseEntity.ok(authService.resetPasswordWithOtp(request));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<com.cropdeal.auth.dto.PasswordResetResponse> resetPassword(
			@Valid @RequestBody com.cropdeal.auth.dto.ResetPasswordRequest request) {
		return ResponseEntity.ok(authService.resetPassword(request));
	}

	/**
	 * JWT authentication is stateless, so logout is completed by removing the token
	 * from the client. This authenticated endpoint gives every role a consistent
	 * logout API and prevents unauthenticated logout calls.
	 */
	@PostMapping("/logout")
	public ResponseEntity<String> logout() {
		return ResponseEntity.ok("Logout successful. Remove the JWT token from the client.");
	}
}
