package com.cropdeal.auth.service;

import com.cropdeal.auth.dto.AuthResponse;
import com.cropdeal.auth.dto.FacebookDemoAccountResponse;
import com.cropdeal.auth.dto.LoginRequest;
import com.cropdeal.auth.dto.MockFacebookLoginRequest;
import com.cropdeal.auth.dto.RegisterRequest;
import com.cropdeal.auth.entity.AuthUser;
import com.cropdeal.auth.entity.Role;
import com.cropdeal.auth.repository.AuthUserRepository;
import com.cropdeal.auth.repository.PasswordResetTokenRepository;
import com.cropdeal.auth.security.JwtService;

import com.cropdeal.auth.dto.MessageResponse;
import com.cropdeal.auth.dto.RequestOtpRequest;
import com.cropdeal.auth.dto.ResetWithOtpRequest;
import com.cropdeal.auth.dto.VerifyOtpRequest;
import com.cropdeal.auth.entity.PasswordResetOtp;
import com.cropdeal.auth.entity.RegistrationOtp;
import com.cropdeal.auth.repository.PasswordResetOtpRepository;
import com.cropdeal.auth.repository.RegistrationOtpRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private record FacebookDemoAccount(String accountId, String displayName, String email, Role role) {
    }

    private static final List<FacebookDemoAccount> FACEBOOK_DEMO_ACCOUNTS = List.of(
            new FacebookDemoAccount(
                    "FB-FARMER-01",
                    "Farmer One",
                    "farmer.one@cropdeal.demo",
                    Role.FARMER
            ),
            new FacebookDemoAccount(
                    "FB-FARMER-02",
                    "Farmer Two",
                    "farmer.two@cropdeal.demo",
                    Role.FARMER
            ),
            new FacebookDemoAccount(
                    "FB-DEALER-01",
                    "Dealer One",
                    "dealer.one@cropdeal.demo",
                    Role.DEALER
            ),
            new FacebookDemoAccount(
                    "FB-DEALER-02",
                    "Dealer Two",
                    "dealer.two@cropdeal.demo",
                    Role.DEALER
            )
    );

    private final AuthUserRepository authUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final RegistrationOtpRepository registrationOtpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthUserRepository authUserRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           PasswordResetOtpRepository passwordResetOtpRepository,
                           RegistrationOtpRepository registrationOtpRepository,
                           EmailService emailService,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.authUserRepository = authUserRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.registrationOtpRepository = registrationOtpRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin cannot be registered publicly");
        }

        if (authUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered. Please sign in instead.");
        }

        // Verify registration OTP
        Optional<RegistrationOtp> verifiedOtp = registrationOtpRepository.findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc(email);
        if (verifiedOtp.isEmpty()) {
            if (request.getOtp() != null && !request.getOtp().isBlank()) {
                RegistrationOtp pendingOtp = registrationOtpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                        .orElseThrow(() -> new IllegalArgumentException("No verification OTP requested for this email. Please request an OTP first."));
                if (pendingOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException("OTP has expired. Please request a new one.");
                }
                if (!passwordEncoder.matches(request.getOtp().trim(), pendingOtp.getOtpHash())) {
                    throw new IllegalArgumentException("Incorrect verification OTP. Please try again.");
                }
                pendingOtp.setVerified(true);
                pendingOtp.setVerifiedAt(LocalDateTime.now());
                registrationOtpRepository.save(pendingOtp);
            } else {
                throw new IllegalArgumentException("Please verify your email using OTP before registration.");
            }
        }

        AuthUser user = new AuthUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        user.setEmailVerified(true);
        user.setCreatedAt(java.time.LocalDateTime.now());

        AuthUser savedUser = authUserRepository.save(user);

        AuthResponse response = new AuthResponse();
        response.setUserId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().name());
        response.setToken(jwtService.generateToken(savedUser));
        response.setMessage("User registered successfully. Use the returned userId as authUserId when creating the profile.");
        return response;
    }

    @Override
    public List<FacebookDemoAccountResponse> getFacebookDemoAccounts() {
        return FACEBOOK_DEMO_ACCOUNTS.stream()
                .map(account -> new FacebookDemoAccountResponse(
                        account.accountId(),
                        account.displayName(),
                        account.email(),
                        account.role().name()
                ))
                .toList();
    }

    @Override
    public AuthResponse mockFacebookLogin(MockFacebookLoginRequest request) {
        FacebookDemoAccount selected = FACEBOOK_DEMO_ACCOUNTS.stream()
                .filter(account -> account.accountId().equalsIgnoreCase(request.getAccountId().trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid dummy Facebook account"));

        if (selected.role() == Role.ADMIN) {
            throw new IllegalArgumentException("Dummy Facebook login supports FARMER and DEALER only");
        }

        AuthUser user = authUserRepository.findByEmail(selected.email())
                .orElseGet(() -> createDemoUser(selected));

        if (user.getRole() != selected.role()) {
            throw new IllegalArgumentException("Dummy Facebook account role does not match the stored user role");
        }

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            authUserRepository.save(user);
        }

        if (!user.isActive()) {
            throw new DisabledException("User account is inactive");
        }

        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setToken(jwtService.generateToken(user));
        response.setMessage("Dummy Facebook authentication successful; no external Facebook verification was used");
        response.setRedirectPath(
                user.getRole() == Role.FARMER ? "/farmer/home" : "/dealer/home"
        );
        return response;
    }

    private AuthUser createDemoUser(FacebookDemoAccount account) {
        AuthUser user = new AuthUser();
        user.setEmail(account.email());
        user.setPassword(passwordEncoder.encode("dummy-facebook-account"));
        user.setRole(account.role());
        user.setActive(true);
        user.setEmailVerified(true);
        user.setCreatedAt(java.time.LocalDateTime.now());
        return authUserRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new DisabledException("Please verify your email using OTP before logging in.");
        }

        if (!user.isActive()) {
            throw new DisabledException("User account is inactive");
        }

        String token = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setMessage("Login successful");
        response.setToken(token);
        return response;
    }

    @Override
    public com.cropdeal.auth.dto.PasswordResetResponse forgotPassword(com.cropdeal.auth.dto.ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account registered with email: " + email));

        if (!user.isActive()) {
            throw new DisabledException("User account is inactive");
        }

        String token = java.util.UUID.randomUUID().toString();
        java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusMinutes(30);

        com.cropdeal.auth.entity.PasswordResetToken resetToken =
                new com.cropdeal.auth.entity.PasswordResetToken(token, user.getEmail(), expiry);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:4200/login?resetToken=" + token;
        return new com.cropdeal.auth.dto.PasswordResetResponse(
                "Secure password reset link generated successfully. Valid for 30 minutes.",
                token,
                resetLink
        );
    }

    @Override
    public com.cropdeal.auth.dto.PasswordResetResponse resetPassword(com.cropdeal.auth.dto.ResetPasswordRequest request) {
        com.cropdeal.auth.entity.PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("This password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("This password reset token has expired. Please request a new one.");
        }

        AuthUser user = authUserRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found for this reset token"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authUserRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new com.cropdeal.auth.dto.PasswordResetResponse(
                "Password has been successfully updated. You may now sign in with your new password."
        );
    }

    @Override
    public MessageResponse requestOtp(RequestOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<AuthUser> userOpt = authUserRepository.findByEmail(email);

        // Security requirement: Do not reveal whether an email exists, to prevent user-enumeration
        if (userOpt.isEmpty()) {
            return new MessageResponse("If the email is registered, an OTP has been sent.");
        }

        AuthUser user = userOpt.get();
        if (!user.isActive()) {
            throw new DisabledException("User account is inactive");
        }

        // Rate limiting cooldown check (30 seconds between requests)
        Optional<PasswordResetOtp> latestOtp = passwordResetOtpRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (latestOtp.isPresent() && !latestOtp.get().isUsed()) {
            LocalDateTime createdAt = latestOtp.get().getCreatedAt();
            if (createdAt.plusSeconds(30).isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Please wait 30 seconds before requesting another OTP.");
            }
        }

        // Generate 6-digit cryptographically secure OTP
        int randomPin = new SecureRandom().nextInt(900000) + 100000;
        String rawOtp = String.valueOf(randomPin);

        // OTP expires in 5 minutes
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Store BCrypt hash of OTP
        String otpHash = passwordEncoder.encode(rawOtp);
        PasswordResetOtp resetOtp = new PasswordResetOtp(user.getId(), email, otpHash, expiresAt);
        PasswordResetOtp savedOtp = passwordResetOtpRepository.save(resetOtp);

        // Send OTP to user's real email address via EmailService
        try {
            emailService.sendOtpEmail(email, rawOtp);
        } catch (Exception e) {
            // Delete pending OTP if delivery failed so invalid/undelivered OTP is not stored
            passwordResetOtpRepository.delete(savedOtp);
            throw new IllegalStateException("Unable to send OTP email. Please try again later.");
        }

        return new MessageResponse("If the email is registered, an OTP has been sent.");
    }

    @Override
    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String inputOtp = request.getOtp().trim();

        PasswordResetOtp resetOtp = passwordResetOtpRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP."));

        if (resetOtp.isUsed()) {
            throw new IllegalArgumentException("OTP has already been used.");
        }

        if (resetOtp.getFailedAttempts() >= 5) {
            throw new IllegalArgumentException("Too many failed attempts. Please request a new OTP.");
        }

        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(inputOtp, resetOtp.getOtpHash())) {
            resetOtp.setFailedAttempts(resetOtp.getFailedAttempts() + 1);
            passwordResetOtpRepository.save(resetOtp);
            throw new IllegalArgumentException("Invalid OTP.");
        }

        // Mark OTP as verified
        resetOtp.setVerifiedAt(LocalDateTime.now());
        passwordResetOtpRepository.save(resetOtp);

        return new MessageResponse("OTP verified successfully");
    }

    @Override
    public MessageResponse resetPasswordWithOtp(ResetWithOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String inputOtp = request.getOtp().trim();

        if (request.getNewPassword() == null || request.getConfirmPassword() == null) {
            throw new IllegalArgumentException("Please enter your new password and confirmation.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        PasswordResetOtp resetOtp = passwordResetOtpRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP."));

        if (resetOtp.isUsed()) {
            throw new IllegalArgumentException("OTP has already been used.");
        }

        if (resetOtp.getFailedAttempts() >= 5) {
            throw new IllegalArgumentException("Too many failed attempts. Please request a new OTP.");
        }

        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(inputOtp, resetOtp.getOtpHash())) {
            resetOtp.setFailedAttempts(resetOtp.getFailedAttempts() + 1);
            passwordResetOtpRepository.save(resetOtp);
            throw new IllegalArgumentException("Invalid OTP.");
        }

        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account registered with email: " + email));

        // Update password using BCrypt
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authUserRepository.save(user);

        // Mark OTP as USED so it can NEVER be reused
        resetOtp.setUsed(true);
        resetOtp.setVerifiedAt(LocalDateTime.now());
        passwordResetOtpRepository.save(resetOtp);

        return new MessageResponse("Password reset successful. You may now log in with your new password.");
    }

    @Override
    public MessageResponse requestRegistrationOtp(RequestOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (authUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("This email address is already registered. Please log in instead.");
        }

        // Rate limiting cooldown check (30 seconds between requests)
        Optional<RegistrationOtp> latestOtp = registrationOtpRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (latestOtp.isPresent() && !latestOtp.get().isVerified()) {
            LocalDateTime createdAt = latestOtp.get().getCreatedAt();
            if (createdAt.plusSeconds(30).isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Please wait 30 seconds before requesting another OTP.");
            }
        }

        // Generate 6-digit cryptographically secure OTP
        int randomPin = new SecureRandom().nextInt(900000) + 100000;
        String rawOtp = String.valueOf(randomPin);

        // OTP expires in 5 minutes
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Store BCrypt hash of OTP
        String otpHash = passwordEncoder.encode(rawOtp);
        RegistrationOtp regOtp = new RegistrationOtp(email, otpHash, expiresAt);
        RegistrationOtp savedOtp = registrationOtpRepository.save(regOtp);

        System.out.println("==================================================");
        System.out.println(">>> REGISTRATION OTP for " + email + ": " + rawOtp);
        System.out.println("==================================================");

        try {
            emailService.sendOtpEmail(email, rawOtp);
        } catch (Exception e) {
            // Development fallback: Keep savedOtp so user can verify via console OTP
            System.err.println("Could not deliver SMTP email: " + e.getMessage());
        }

        return new MessageResponse("Verification OTP has been sent to " + email + ". Valid for 5 minutes.");
    }

    @Override
    public MessageResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String inputOtp = request.getOtp().trim();

        RegistrationOtp regOtp = registrationOtpRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("No OTP found for this email. Please request an OTP first."));

        if (regOtp.isVerified()) {
            return new MessageResponse("Email is already verified. You can proceed with registration.");
        }

        if (regOtp.getFailedAttempts() >= 5) {
            throw new IllegalArgumentException("Too many failed attempts. Please request a new OTP.");
        }

        if (regOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(inputOtp, regOtp.getOtpHash())) {
            regOtp.setFailedAttempts(regOtp.getFailedAttempts() + 1);
            registrationOtpRepository.save(regOtp);
            throw new IllegalArgumentException("Incorrect OTP. Please enter the valid 6-digit OTP.");
        }

        regOtp.setVerified(true);
        regOtp.setVerifiedAt(LocalDateTime.now());
        registrationOtpRepository.save(regOtp);

        return new MessageResponse("Email successfully verified! You may now complete account registration.");
    }
}
