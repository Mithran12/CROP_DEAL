package com.cropdeal.user.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtTestTokenGenerator {

    public static void main(String[] args) {

        String secret =
                "CropDealSecretKeyForJwtAuthenticationMustBeAtLeast32CharactersLong";

        SecretKey key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        String token = Jwts.builder()
                .subject("farmer@gmail.com")
                .claim("role", "ADMIN")
                .claim("authUserId", 999L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        System.out.println();
        System.out.println("==========================================");
        System.out.println("TEMPORARY FARMER JWT TOKEN");
        System.out.println("==========================================");
        System.out.println(token);
        System.out.println("==========================================");
    }
}