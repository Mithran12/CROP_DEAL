package com.cropdeal.order.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtUserDetailsTest {

	@Test
	void shouldExtractUserDetailsFromJwt() {

		Jwt jwt = new Jwt("test-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "HS256"),
				Map.of("sub", "dealer@example.com", "userId", 123L, "role", "DEALER"));

		JwtUserDetails userDetails = JwtUserDetails.fromJwt(jwt);

		assertEquals(123L, userDetails.getUserId());

		assertEquals("DEALER", userDetails.getRole());

		assertEquals("dealer@example.com", userDetails.getEmail());
	}

	@Test
	void shouldExtractFarmerDetailsFromJwt() {

		Jwt jwt = new Jwt("test-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "HS256"),
				Map.of("sub", "farmer@example.com", "userId", 456L, "role", "FARMER"));

		JwtUserDetails userDetails = JwtUserDetails.fromJwt(jwt);

		assertEquals(456L, userDetails.getUserId());

		assertEquals("FARMER", userDetails.getRole());

		assertEquals("farmer@example.com", userDetails.getEmail());
	}

	@Test
	void shouldExtractAdminDetailsFromJwt() {

		Jwt jwt = new Jwt("test-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "HS256"),
				Map.of("sub", "admin@cropdeal.com", "userId", 1L, "role", "ADMIN"));

		JwtUserDetails userDetails = JwtUserDetails.fromJwt(jwt);

		assertEquals(1L, userDetails.getUserId());

		assertEquals("ADMIN", userDetails.getRole());

		assertEquals("admin@cropdeal.com", userDetails.getEmail());
	}
}