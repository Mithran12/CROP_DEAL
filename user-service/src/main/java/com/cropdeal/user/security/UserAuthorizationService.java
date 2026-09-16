package com.cropdeal.user.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthorizationService {

    private final JwtService jwtService;

    public UserAuthorizationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Long getCurrentAuthUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getCredentials() == null) {
            return null;
        }

        return jwtService.extractAuthUserId(authentication.getCredentials().toString());
        
    }

    public boolean isAdmin() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null && authentication.getAuthorities().stream().anyMatch(authority ->authority.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isOwner(Long authUserId) {

        Long currentAuthUserId = getCurrentAuthUserId();

        return currentAuthUserId != null && currentAuthUserId.equals(authUserId);
    }

    public boolean canAccess(Long authUserId) {

        return isAdmin() || isOwner(authUserId);
    }
}