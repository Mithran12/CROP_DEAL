package com.cropdeal.crop.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAuthorizationService {

    private final JwtService jwtService;

    public boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    public boolean canAccess(Long farmerId) {

        if (isAdmin()) {
            return true;
        }

        Long authUserId = getAuthenticatedUserId();

        return authUserId != null
                && authUserId.equals(farmerId);
    }

    public Long getAuthenticatedUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object credentials = authentication.getCredentials();

        if (!(credentials instanceof String token)) {
            return null;
        }

        try {
            return jwtService.extractAuthUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}