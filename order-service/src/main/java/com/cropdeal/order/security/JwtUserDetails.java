package com.cropdeal.order.security;

import org.springframework.security.oauth2.jwt.Jwt;

public class JwtUserDetails {

	private final Long userId;
	private final String role;
	private final String email;

	public JwtUserDetails(Long userId, String role, String email) {

		this.userId = userId;
		this.role = role;
		this.email = email;
	}

	public Long getUserId() {
		return userId;
	}

	public String getRole() {
		return role;
	}

	public String getEmail() {
		return email;
	}

	public static JwtUserDetails fromJwt(Jwt jwt) {

		Long userId = ((Number) jwt.getClaim("userId")).longValue();

		String role = jwt.getClaimAsString("role");

		String email = jwt.getSubject();

		return new JwtUserDetails(userId, role, email);
	}
}