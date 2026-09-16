package com.cropdeal.order.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=cropdeal-test-secret-key-1234567890"
})
class SecurityAuthorizationTest {

    private static final String TEST_SECRET =
            "cropdeal-test-secret-key-1234567890";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    // =========================================================
    // BASIC AUTHORIZATION TESTS
    // =========================================================

    @Test
    void dealerShouldAccessDealerOrders() throws Exception {

        String token =
                createToken(
                        "dealer@example.com",
                        123L,
                        "DEALER");

        mockMvc.perform(
                get("/api/orders/dealer/123")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void farmerShouldNotAccessDealerOrders() throws Exception {

        String token =
                createToken(
                        "farmer@example.com",
                        456L,
                        "FARMER");

        mockMvc.perform(
                get("/api/orders/dealer/123")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void farmerShouldAccessFarmerOrders() throws Exception {

        String token =
                createToken(
                        "farmer@example.com",
                        456L,
                        "FARMER");

        mockMvc.perform(
                get("/api/orders/farmer/456")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void dealerShouldNotAccessFarmerOrders() throws Exception {

        String token =
                createToken(
                        "dealer@example.com",
                        123L,
                        "DEALER");

        mockMvc.perform(
                get("/api/orders/farmer/456")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldNotAccessDealerOrders() throws Exception {

        String token =
                createToken(
                        "admin@cropdeal.com",
                        1L,
                        "ADMIN");

        mockMvc.perform(
                get("/api/orders/dealer/123")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldNotAccessFarmerOrders() throws Exception {

        String token =
                createToken(
                        "admin@cropdeal.com",
                        1L,
                        "ADMIN");

        mockMvc.perform(
                get("/api/orders/farmer/456")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // AUTHENTICATION TEST
    // =========================================================

    @Test
    void requestWithoutJwt_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/orders/dealer/301"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // 9.7.4 ROLE RESTRICTION TESTS
    // =========================================================

    @Test
    void farmerShouldNotCreateOrder()
            throws Exception {

        String token =
                createToken(
                        "farmer@example.com",
                        456L,
                        "FARMER");

        mockMvc.perform(
                post("/api/orders")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldNotCreateOrder()
            throws Exception {

        String token =
                createToken(
                        "admin@cropdeal.com",
                        1L,
                        "ADMIN");

        mockMvc.perform(
                post("/api/orders")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void farmerShouldNotCancelOrder()
            throws Exception {

        String token =
                createToken(
                        "farmer@example.com",
                        456L,
                        "FARMER");

        mockMvc.perform(
                patch("/api/orders/1/cancel")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldNotCancelOrder()
            throws Exception {

        String token =
                createToken(
                        "admin@cropdeal.com",
                        1L,
                        "ADMIN");

        mockMvc.perform(
                patch("/api/orders/1/cancel")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void farmerShouldNotCompleteOrder()
            throws Exception {

        String token =
                createToken(
                        "farmer@example.com",
                        456L,
                        "FARMER");

        mockMvc.perform(
                patch("/api/orders/1/complete")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldNotCompleteOrder()
            throws Exception {

        String token =
                createToken(
                        "admin@cropdeal.com",
                        1L,
                        "ADMIN");

        mockMvc.perform(
                patch("/api/orders/1/complete")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // JWT CREATION HELPER
    // =========================================================

    private String createToken(
            String email,
            Long userId,
            String role)
            throws Exception {

        JWTClaimsSet claims =
                new JWTClaimsSet.Builder()
                        .subject(email)
                        .claim("userId", userId)
                        .claim("role", role)
                        .issueTime(new Date())
                        .expirationTime(
                                Date.from(
                                        Instant.now()
                                                .plusSeconds(3600)))
                        .build();

        SignedJWT signedJWT =
                new SignedJWT(
                        new JWSHeader(JWSAlgorithm.HS256),
                        claims);

        signedJWT.sign(
                new MACSigner(
                        TEST_SECRET.getBytes(
                                StandardCharsets.UTF_8)));

        return signedJWT.serialize();
    }
}