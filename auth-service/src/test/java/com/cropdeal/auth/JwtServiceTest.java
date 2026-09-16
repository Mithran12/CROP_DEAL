package com.cropdeal.auth;

import com.cropdeal.auth.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void generatedTokenCanBeParsed() {
        JwtService service = new JwtService(
                "CropDealSecretKeyForJwtAuthentication2026Secure", 1800000);

        String token = service.generateToken("farmer@example.com", "FARMER");
        var claims = service.parseToken(token);

        assertEquals("farmer@example.com", claims.getSubject());
        assertEquals("FARMER", claims.get("role", String.class));
    }
}
