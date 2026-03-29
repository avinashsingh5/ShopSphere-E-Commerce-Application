package com.shopsphere.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "eureka.client.enabled=false",
        "jwt.secret=ShopSphereSecretKey2024ShopSphereSecretKey2024ShopSphere",
        "jwt.expiration=86400000"
})
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken("test@test.com", "CUSTOMER", "Test User");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsername() {
        String token = jwtService.generateToken("test@test.com", "CUSTOMER", "Test User");
        String username = jwtService.extractUsername(token);
        assertEquals("test@test.com", username);
    }

    @Test
    void shouldExtractRole() {
        String token = jwtService.generateToken("admin@test.com", "ADMIN", "Admin User");
        String role = jwtService.extractRole(token);
        assertEquals("ADMIN", role);
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtService.generateToken("test@test.com", "CUSTOMER", "Test User");
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldInvalidateInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }
}
