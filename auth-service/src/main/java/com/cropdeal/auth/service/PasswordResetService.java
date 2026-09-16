package com.cropdeal.auth.service;

import com.cropdeal.auth.entity.PasswordResetToken;
import com.cropdeal.auth.entity.User;
import com.cropdeal.auth.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository repository;

    public PasswordResetService(PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    public String createToken(User user) {

        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setToken(UUID.randomUUID().toString());

        resetToken.setUser(user);

        resetToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(15)
        );

        return repository.save(resetToken).getToken();
    }

    public PasswordResetToken findValidToken(String token) {

        PasswordResetToken resetToken = repository.findByToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid reset token"
                        ));

        if (resetToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            repository.delete(resetToken);

            throw new IllegalArgumentException(
                    "Reset token expired"
            );
        }

        return resetToken;
    }

    public void delete(PasswordResetToken token) {
        repository.delete(token);
    }
}