package com.cropdeal.auth.service;

import com.cropdeal.auth.dto.DealerRegisterRequest;
import com.cropdeal.auth.dto.FarmerRegisterRequest;
import com.cropdeal.auth.dto.LoginRequest;
import com.cropdeal.auth.dto.TokenResponse;
import com.cropdeal.auth.entity.RefreshToken;
import com.cropdeal.auth.entity.Role;
import com.cropdeal.auth.entity.User;
import com.cropdeal.auth.repository.UserRepository;
import com.cropdeal.auth.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordResetService passwordResetService,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetService = passwordResetService;
        this.emailService = emailService;
    }

    public User registerFarmer(FarmerRegisterRequest request) {
        return register(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                Role.FARMER
        );
    }

    public User registerDealer(DealerRegisterRequest request) {
        return register(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                Role.DEALER
        );
    }

    private User register(
            String name,
            String email,
            String password,
            Role role) {

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);

        return userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {

        System.out.println("LOGIN 1: Starting login");

        User user = userRepository
                .findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        ));

        System.out.println("LOGIN 2: User found = " + user.getEmail());

        if (!user.isActive()) {
            throw new IllegalStateException("Account is inactive");
        }

        System.out.println("LOGIN 3: User is active");

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            System.out.println("LOGIN 4: Password does NOT match");

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        System.out.println("LOGIN 4: Password matches");

        TokenResponse response = issueTokens(user);

        System.out.println("LOGIN 5: Tokens generated");

        return response;
    }

    public TokenResponse refreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenService.findByToken(token);

        if (!refreshTokenService.isValid(refreshToken)) {

            refreshTokenService.deleteByUserId(
                    refreshToken.getUser().getId()
            );

            throw new IllegalArgumentException(
                    "Refresh token expired"
            );
        }

        User user = refreshToken.getUser();

        if (!user.isActive()) {
            throw new IllegalStateException(
                    "Account is inactive"
            );
        }

        return new TokenResponse(
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole().name()
                ),
                refreshToken.getToken()
        );
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteToken(refreshToken);
    }

    public String createPasswordResetToken(String email) {

        User user = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No account found for this email"
                        ));

        String token = passwordResetService.createToken(user);

        String resetLink =
                "http://localhost:4200/reset-password?token=" + token;

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                resetLink
        );

        return "Password reset email sent successfully";
    }

    public void resetPassword(
            String token,
            String newPassword) {

        var resetToken =
                passwordResetService.findValidToken(token);

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        passwordResetService.delete(resetToken);
    }

    public User findOrCreateFederatedUser(
            String email,
            String name,
            Role defaultRole) {

        String normalizedEmail =
                email.trim().toLowerCase();

        return userRepository
                .findByEmail(normalizedEmail)
                .map(user -> {

                    if (!user.isActive()) {
                        throw new IllegalStateException(
                                "Account is inactive"
                        );
                    }

                    if (name != null && !name.isBlank()) {
                        user.setName(name);
                    }

                    return userRepository.save(user);
                })
                .orElseGet(() -> {

                    User user = new User();

                    user.setName(
                            name == null || name.isBlank()
                                    ? "Facebook User"
                                    : name
                    );

                    user.setEmail(normalizedEmail);

                    user.setPassword(
                            passwordEncoder.encode(
                                    java.util.UUID
                                            .randomUUID()
                                            .toString()
                            )
                    );

                    user.setRole(defaultRole);
                    user.setActive(true);

                    return userRepository.save(user);
                });
    }

    public User setActiveStatus(
            Long id,
            boolean active) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        ));

        user.setActive(active);

        return userRepository.save(user);
    }

    public User getUser(Long id) {

        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        ));
    }

    private TokenResponse issueTokens(User user) {

        System.out.println(
                "TOKEN 1: Starting token generation"
        );

        String accessToken =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole().name()
                );

        System.out.println(
                "TOKEN 2: Access token generated"
        );

        String refreshToken =
                refreshTokenService
                        .createRefreshToken(user)
                        .getToken();

        System.out.println(
                "TOKEN 3: Refresh token generated"
        );

        return new TokenResponse(
                accessToken,
                refreshToken
        );
    }
}