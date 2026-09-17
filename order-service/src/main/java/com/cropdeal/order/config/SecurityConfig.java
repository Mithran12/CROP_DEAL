package com.cropdeal.order.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${cropdeal.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // PUBLIC ENDPOINTS

                .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()

                // PURCHASE REQUEST

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/purchase-requests"
                ).hasRole("DEALER")

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/purchase-requests/*/accept",
                        "/api/purchase-requests/*/reject"
                ).hasRole("FARMER")

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/purchase-requests/*/cancel"
                ).hasRole("DEALER")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/purchase-requests/**"
                ).hasAnyRole(
                        "FARMER",
                        "DEALER")

                // NEGOTIATION

                .requestMatchers(
                        "/api/negotiations/**"
                ).hasAnyRole(
                        "FARMER",
                        "DEALER")

                // ORDERS

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/orders"
                ).hasRole("DEALER")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/orders/farmer/**"
                ).hasRole("FARMER")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/orders/dealer/**"
                ).hasRole("DEALER")

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/orders/*/cancel"
                ).hasRole("DEALER")

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/orders/*/complete"
                ).hasRole("DEALER")

                // Order status can be called by an authenticated
                // farmer or dealer. Service layer checks ownership.
                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/orders/*/status"
                ).hasAnyRole(
                        "FARMER",
                        "DEALER")

                // INDIVIDUAL RESOURCE ACCESS

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/orders/**"
                ).hasAnyRole(
                        "FARMER",
                        "DEALER")

                // EVERYTHING ELSE

                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                            jwtAuthenticationConverter()
                    )
                )
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter);

        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey secretKey =
                new SecretKeySpec(
                        jwtSecret.getBytes(),
                        "HmacSHA256");

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}