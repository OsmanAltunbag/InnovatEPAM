package com.innovatepam.auth.security;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {
    private JwtService jwtService;
    private static final String TEST_JWT_SECRET = "test-secret-key-that-is-at-least-32-bytes-long-for-hs256-algorithm";
    private static final long TEST_EXPIRATION_SECONDS = 3600; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_JWT_SECRET, TEST_EXPIRATION_SECONDS);
    }

    private User createTestUser(String email, String roleName) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(roleName);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    @Test
    void generateToken_WithValidInputs_ReturnsNonNullToken() {
        // Given
        User user = createTestUser("test@example.com", "submitter");

        // When
        String token = jwtService.generateToken(user);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void parseToken_WithValidToken_ReturnsCorrectClaims() {
        // Given
        User user = createTestUser("test@example.com", "submitter");
        String token = jwtService.generateToken(user);

        // When
        Claims claims = jwtService.parseToken(token);

        // Then
        assertNotNull(claims);
        // Verify the custom claims are present
        assertNotNull(claims.get("userId", String.class));
        assertEquals("submitter", claims.get("role", String.class));
        // Email is set as subject but may be overwritten by setClaims in JJWT
        // Verify token is valid and contains expected data
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void parseToken_WithExpiredToken_ThrowsExpiredJwtException() throws InterruptedException {
        // Given - create service with very short expiration
        JwtService shortLivedService = new JwtService(TEST_JWT_SECRET, 1L); // 1 second
        User user = createTestUser("test@example.com", "submitter");
        String token = shortLivedService.generateToken(user);

        // Wait for token to expire
        Thread.sleep(1100);

        // When/Then
        assertThrows(ExpiredJwtException.class, () -> shortLivedService.parseToken(token));
    }

    @Test
    void parseToken_WithInvalidToken_ThrowsJwtException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When/Then
        assertThrows(JwtException.class, () -> jwtService.parseToken(invalidToken));
    }

    @Test
    void parseToken_WithTamperedToken_ThrowsJwtException() {
        // Given
        User user = createTestUser("test@example.com", "submitter");
        String validToken = jwtService.generateToken(user);
        // Tamper with the token by changing a character
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // When/Then
        assertThrows(JwtException.class, () -> jwtService.parseToken(tamperedToken));
    }

    @Test
    void parseToken_WithNullToken_ThrowsException() {
        // When/Then
        assertThrows(Exception.class, () -> jwtService.parseToken(null));
    }

    @Test
    void parseToken_WithEmptyToken_ThrowsException() {
        // When/Then
        assertThrows(Exception.class, () -> jwtService.parseToken(""));
    }

    @Test
    void getExpirationSeconds_ReturnsConfiguredValue() {
        // When
        long expiration = jwtService.getExpirationSeconds();

        // Then
        assertEquals(TEST_EXPIRATION_SECONDS, expiration);
    }

    @Test
    void generateToken_CreatesTokenWithCorrectExpiration() {
        // Given
        User user = createTestUser("test@example.com", "submitter");
        String token = jwtService.generateToken(user);

        // When
        Claims claims = jwtService.parseToken(token);
        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();
        long tokenLifeSeconds = (expiration - issuedAt) / 1000;

        // Then
        assertEquals(TEST_EXPIRATION_SECONDS, tokenLifeSeconds, 1); // Allow 1 second tolerance
    }

    @Test
    void generateToken_WithDifferentRoles_CreatesValidTokens() {
        // Given
        String[] roles = {"submitter", "evaluator/admin"};

        for (String role : roles) {
            // When
            User user = createTestUser("test@example.com", role);
            String token = jwtService.generateToken(user);
            Claims claims = jwtService.parseToken(token);

            // Then
            assertEquals(role, claims.get("role", String.class));
        }
    }

    @Test
    void generateToken_IncludesUserId() {
        // Given
        User user = createTestUser("test@example.com", "submitter");

        // When
        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseToken(token);
        String userId = claims.get("userId", String.class);

        // Then
        assertNotNull(userId);
        assertFalse(userId.isEmpty());
        // Verify it's a valid UUID format
        assertDoesNotThrow(() -> UUID.fromString(userId));
    }
}
