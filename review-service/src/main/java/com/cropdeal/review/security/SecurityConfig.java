package com.cropdeal.review.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	private final JwtAuthenticationFilter filter;

	public SecurityConfig(JwtAuthenticationFilter f) {
		filter = f;
	}

	@Bean
	SecurityFilterChain chain(HttpSecurity h) throws Exception {
		return h.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a.requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**")
						.permitAll().anyRequest().authenticated())
				.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
	}
}
