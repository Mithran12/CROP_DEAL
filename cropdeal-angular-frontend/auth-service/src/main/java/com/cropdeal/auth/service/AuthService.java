package com.cropdeal.auth.service;

import com.cropdeal.auth.dto.AuthResponse;
import com.cropdeal.auth.dto.FacebookDemoAccountResponse;
import com.cropdeal.auth.dto.LoginRequest;
import com.cropdeal.auth.dto.MockFacebookLoginRequest;
import com.cropdeal.auth.dto.RegisterRequest;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    List<FacebookDemoAccountResponse> getFacebookDemoAccounts();

    AuthResponse mockFacebookLogin(MockFacebookLoginRequest request);

    com.cropdeal.auth.dto.PasswordResetResponse forgotPassword(com.cropdeal.auth.dto.ForgotPasswordRequest request);

    com.cropdeal.auth.dto.PasswordResetResponse resetPassword(com.cropdeal.auth.dto.ResetPasswordRequest request);

    com.cropdeal.auth.dto.MessageResponse requestOtp(com.cropdeal.auth.dto.RequestOtpRequest request);

    com.cropdeal.auth.dto.MessageResponse verifyOtp(com.cropdeal.auth.dto.VerifyOtpRequest request);

    com.cropdeal.auth.dto.MessageResponse resetPasswordWithOtp(com.cropdeal.auth.dto.ResetWithOtpRequest request);

    com.cropdeal.auth.dto.MessageResponse requestRegistrationOtp(com.cropdeal.auth.dto.RequestOtpRequest request);

    com.cropdeal.auth.dto.MessageResponse verifyRegistrationOtp(com.cropdeal.auth.dto.VerifyOtpRequest request);
}
