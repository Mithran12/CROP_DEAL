package com.cropdeal.auth.service;

import com.cropdeal.auth.entity.RefreshToken;
import com.cropdeal.auth.entity.User;
import com.cropdeal.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByToken(String token) {

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token"));
    }

    public boolean isValid(RefreshToken refreshToken) {

        return refreshToken.getExpiryDate()
                .isAfter(LocalDateTime.now());
    }

    public void deleteByUserId(Long userId) {

    	refreshTokenRepository.deleteByUser_Id(userId);
    }

    public void deleteToken(String token) {

        RefreshToken refreshToken = findByToken(token);

        refreshTokenRepository.delete(refreshToken);
    }
}