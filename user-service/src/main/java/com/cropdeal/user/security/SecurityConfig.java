package com.cropdeal.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/users/farmers/*/activate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/farmers/*/deactivate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/farmers/**").hasRole("ADMIN")
                .requestMatchers("/api/users/farmers/**").hasAnyRole("FARMER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/dealers/*/activate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/dealers/*/deactivate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/dealers/**").hasRole("ADMIN")
                .requestMatchers("/api/users/dealers/**").hasAnyRole("DEALER", "ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}