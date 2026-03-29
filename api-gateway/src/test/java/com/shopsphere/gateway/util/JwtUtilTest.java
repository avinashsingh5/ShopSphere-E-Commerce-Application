package com.shopsphere.gateway.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "ShopSphereSecretKey2024ShopSphereSecretKey2024ShopSphere";

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, SECRET);
    }

    private String generateTestToken(String email, String role) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setClaims(Map.of("role", role, "name", "Test User"))
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    @Test
    void validateToken_ShouldNotThrow_ForValidToken() {
        String token = generateTestToken("test@test.com", "CUSTOMER");
        assertDoesNotThrow(() -> jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_ShouldThrow_ForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void extractUsername_ShouldReturnSubject() {
        String token = generateTestToken("admin@test.com", "ADMIN");
        assertEquals("admin@test.com", jwtUtil.extractUsername(token));
    }

    @Test
    void extractRole_ShouldReturnRole() {
        String token = generateTestToken("admin@test.com", "ADMIN");
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void extractRole_ShouldReturnCustomerRole() {
        String token = generateTestToken("customer@test.com", "CUSTOMER");
        assertEquals("CUSTOMER", jwtUtil.extractRole(token));
    }

    @Test
    void validateToken_ShouldThrow_ForExpiredToken() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String expiredToken = Jwts.builder()
                .setClaims(Map.of("role", "CUSTOMER"))
                .setSubject("test@test.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 200000))
                .setExpiration(new Date(System.currentTimeMillis() - 100000))
                .signWith(key)
                .compact();

        assertThrows(Exception.class, () -> jwtUtil.validateToken(expiredToken));
    }
}
