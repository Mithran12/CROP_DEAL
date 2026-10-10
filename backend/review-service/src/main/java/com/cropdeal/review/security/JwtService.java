package com.cropdeal.review.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
	@Value("${jwt.secret}")
	private String secret;

	private Claims claims(String t) {
		return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).build()
				.parseSignedClaims(t).getPayload();
	}

	public boolean valid(String t) {
		try {
			return claims(t).getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	public String claimsEmail(String t) {
		return claims(t).getSubject();
	}

	public Long userId(String t) {
		return claims(t).get("userId", Long.class);
	}

	public String role(String t) {
		return claims(t).get("role", String.class);
	}
}
