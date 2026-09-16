package com.cropdeal.order.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

class SecurityConfigTest {

	private static final String TEST_SECRET = "cropdeal-test-secret-key-1234567890";

	private JwtDecoder jwtDecoder;

	@BeforeEach
	void setUp() {

		SecurityConfig securityConfig = new SecurityConfig();

		ReflectionTestUtils.setField(securityConfig, "jwtSecret", TEST_SECRET);

		jwtDecoder = securityConfig.jwtDecoder();
	}

	@Test
	void validJwtShouldBeDecodedSuccessfully() throws Exception {

		String token = createToken("dealer@example.com", 123L, "DEALER", Instant.now().plusSeconds(3600), TEST_SECRET);

		Jwt jwt = jwtDecoder.decode(token);

		assertEquals("dealer@example.com", jwt.getSubject());

		assertEquals(123L, ((Number) jwt.getClaim("userId")).longValue());

		assertEquals("DEALER", jwt.getClaimAsString("role"));
	}

	@Test
	void invalidSignatureShouldBeRejected() throws Exception {

		String token = createToken("dealer@example.com", 123L, "DEALER", Instant.now().plusSeconds(3600),
				"another-test-secret-key-1234567890");

		assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
	}

	@Test
	void expiredJwtShouldBeRejected() throws Exception {

		String token = createToken("dealer@example.com", 123L, "DEALER", Instant.now().minusSeconds(60), TEST_SECRET);

		assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
	}

	@Test
	void adminJwtShouldBeDecodedSuccessfully() throws Exception {

		String token = createToken("admin@cropdeal.com", 1L, "ADMIN", Instant.now().plusSeconds(3600), TEST_SECRET);

		Jwt jwt = jwtDecoder.decode(token);

		assertEquals("admin@cropdeal.com", jwt.getSubject());

		assertEquals(1L, ((Number) jwt.getClaim("userId")).longValue());

		assertEquals("ADMIN", jwt.getClaimAsString("role"));
	}

	private String createToken(String email, Long userId, String role, Instant expiration, String secret)
			throws Exception {

		JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(email).claim("userId", userId).claim("role", role)
				.issueTime(new Date()).expirationTime(Date.from(expiration)).build();

		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

		signedJWT.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));

		return signedJWT.serialize();
	}
}