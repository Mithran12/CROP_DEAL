package com.cropdeal.user.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            jwtToken = authorizationHeader.substring(7);

            try {
                username = jwtService.extractUsername(jwtToken);

                String role = jwtService.extractRole(jwtToken);

                if (username != null && SecurityContextHolder .getContext() .getAuthentication() == null && jwtService.isTokenValid(jwtToken)) {

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    jwtToken,
                                    java.util.List.of(authority)
                            );
                        
                    SecurityContextHolder .getContext() .setAuthentication(authentication);
                }

            } catch (Exception exception) {

                SecurityContextHolder.clearContext();

            }
        }

        filterChain.doFilter(request, response);
    }
}