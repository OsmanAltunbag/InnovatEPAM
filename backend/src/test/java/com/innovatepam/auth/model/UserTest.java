package com.innovatepam.auth.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("submitter");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$12$hash");
        user.setRole(role);
        user.setLocked(false);
    }

    @Test
    void isAccountLocked_WithUnlockedAccount_ReturnsFalse() {
        // Given
        user.setLocked(false);

        // When
        boolean locked = user.isAccountLocked();

        // Then
        assertFalse(locked);
    }

    @Test
    void isAccountLocked_WithLockedButNoExpiry_ReturnsFalse() {
        // Given
        user.setLocked(true);
        user.setLockedUntil(null);

        // When
        boolean locked = user.isAccountLocked();

        // Then
        assertFalse(locked);
    }

    @Test
    void isAccountLocked_WithLockedAndFutureExpiry_ReturnsTrue() {
        // Given
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));

        // When
        boolean locked = user.isAccountLocked();

        // Then
        assertTrue(locked);
    }

    @Test
    void isAccountLocked_WithLockedAndPastExpiry_ReturnsFalse() {
        // Given
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(10));

        // When
        boolean locked = user.isAccountLocked();

        // Then
        assertFalse(locked);
    }

    @Test
    void isAccountLocked_WithLockedAndNearExpiry_ReturnsTrue() {
        // Given
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusSeconds(5));

        // When
        boolean locked = user.isAccountLocked();

        // Then
        assertTrue(locked);
    }

    @Test
    void isAccountLocked_WithLockedAndJustExpired_ReturnsFalse() {
        // Given
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().minusSeconds(1));

        // When
        boolean locked = user.isAccountLocked();

        // Then
        assertFalse(locked);
    }

    @Test
    void setLocked_WithTrueValue_LockAccount() {
        // When
        user.setLocked(true);

        // Then
        assertTrue(user.isLocked());
    }

    @Test
    void setLockedUntil_WithFutureDate_SetsExpiry() {
        // Given
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

        // When
        user.setLockedUntil(expiry);

        // Then
        assertEquals(expiry, user.getLockedUntil());
    }

    @Test
    void lockAccount_SetsLockedAndExpiryTogether() {
        // Given
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);

        // When
        user.setLocked(true);
        user.setLockedUntil(lockUntil);

        // Then
        assertTrue(user.isAccountLocked());
    }

    @Test
    void unlockAccount_ClearsLockedStatus() {
        // Given
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));

        // When
        user.setLocked(false);
        user.setLockedUntil(null);

        // Then
        assertFalse(user.isAccountLocked());
    }

    @Test
    void getId_ReturnsAssignedUUID() {
        // Given
        UUID expectedId = UUID.randomUUID();
        user.setId(expectedId);

        // When
        UUID actualId = user.getId();

        // Then
        assertEquals(expectedId, actualId);
    }

    @Test
    void getEmail_ReturnsAssignedEmail() {
        // When
        String email = user.getEmail();

        // Then
        assertEquals("test@example.com", email);
    }

    @Test
    void getPasswordHash_ReturnsAssignedHash() {
        // When
        String hash = user.getPasswordHash();

        // Then
        assertEquals("$2a$12$hash", hash);
    }

    @Test
    void getRole_ReturnsAssignedRole() {
        // When
        Role retrievedRole = user.getRole();

        // Then
        assertEquals(role, retrievedRole);
    }

    @Test
    void setEmail_ChangesEmailValue() {
        // When
        user.setEmail("new@example.com");

        // Then
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void setPasswordHash_ChangesHashValue() {
        // When
        user.setPasswordHash("$2a$12$newhash");

        // Then
        assertEquals("$2a$12$newhash", user.getPasswordHash());
    }

    @Test
    void isAccountLocked_WithAllConditionsMet_ReturnsTrue() {
        // Given - All conditions for account lock
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(15));

        // When
        boolean locked = user.isAccountLocked();

        // Then
        assertTrue(locked);
    }

    @Test
    void isAccountLocked_WithAnyConditionFailing_ReturnsFalse() {
        // Test each failing condition separately
        
        // Condition 1: Not locked flag
        user.setLocked(false);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        assertFalse(user.isAccountLocked());
        
        // Condition 2: No expiry date
        user.setLocked(true);
        user.setLockedUntil(null);
        assertFalse(user.isAccountLocked());
        
        // Condition 3: Expiry in past
        user.setLockedUntil(LocalDateTime.now().minusMinutes(10));
        assertFalse(user.isAccountLocked());
    }

    @Test
    void lockoutDuration_CanBeSpecifiedAndRetrieved() {
        // Given
        LocalDateTime lockTime = LocalDateTime.now();
        LocalDateTime unlockTime = lockTime.plusMinutes(30);

        // When
        user.setLocked(true);
        user.setLockedUntil(unlockTime);

        // Then
        assertNotNull(user.getLockedUntil());
        assertEquals(unlockTime, user.getLockedUntil());
    }

    @Test
    void multipleUnlockAttempts_WorkCorrectly() {
        // Given
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        assertTrue(user.isAccountLocked());

        // When - unlock
        user.setLocked(false);
        user.setLockedUntil(null);
        assertFalse(user.isAccountLocked());

        // When - relock
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        assertTrue(user.isAccountLocked());
    }
}
