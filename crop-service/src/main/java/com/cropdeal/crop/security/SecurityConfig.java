package com.cropdeal.crop.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()

                .requestMatchers(
                        "/actuator/health",
                        "/actuator/info"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/crops"
                ).hasAnyRole("FARMER", "ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/crops/**"
                ).hasAnyRole("FARMER", "ADMIN")

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/crops/*/publish"
                ).hasAnyRole("FARMER", "ADMIN")

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/crops/*/unpublish"
                ).hasAnyRole("FARMER", "ADMIN")

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/crops/*/quantity"
                ).hasAnyRole("FARMER", "ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/crops/**"
                ).hasAnyRole("FARMER", "ADMIN")

         
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/crops/**"
                ).hasAnyRole(
                        "FARMER",
                        "DEALER",
                        "ADMIN"
                )

                .anyRequest().authenticated()
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}