package com.innovatepam.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.innovatepam.auth.model.AuthenticationAttempt;
import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.AuthenticationAttemptRepository;
import com.innovatepam.auth.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationAttemptServiceTest {
    @Mock
    private AuthenticationAttemptRepository attemptRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<AuthenticationAttempt> attemptCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private AuthenticationAttemptService attemptService;

    @BeforeEach
    void setUp() {
        attemptService = new AuthenticationAttemptService(attemptRepository, userRepository);
    }

    private User createTestUser(String email) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("submitter");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setRole(role);
        user.setLocked(false);
        return user;
    }

    @Test
    void recordSuccess_WithValidEmail_SavesSuccessAttempt() {
        // Given
        String email = "test@example.com";
        String ipAddress = "192.168.1.1";

        // When
        attemptService.recordSuccess(email, ipAddress);

        // Then
        verify(attemptRepository).save(attemptCaptor.capture());
        AuthenticationAttempt saved = attemptCaptor.getValue();
        assertEquals(email, saved.getEmail());
        assertTrue(saved.isSuccess());
        assertEquals(ipAddress, saved.getIpAddress());
    }

    @Test
    void recordFailure_WithLessThanMaxAttempts_DoesNotLockAccount() {
        // Given
        User user = createTestUser("test@example.com");
        when(attemptRepository.findRecentFailedAttempts(eq(user.getEmail()), any(LocalDateTime.class)))
            .thenReturn(createFailedAttempts(3)); // 3 attempts, max is 5

        // When
        boolean locked = attemptService.recordFailure(user, "192.168.1.1");

        // Then
        assertFalse(locked);
        verify(attemptRepository).save(attemptCaptor.capture());
        AuthenticationAttempt saved = attemptCaptor.getValue();
        assertFalse(saved.isSuccess());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void recordFailure_WithMaxAttempts_LocksAccount() {
        // Given
        User user = createTestUser("test@example.com");
        when(attemptRepository.findRecentFailedAttempts(eq(user.getEmail()), any(LocalDateTime.class)))
            .thenReturn(createFailedAttempts(5)); // 5 attempts, max is 5

        // When
        boolean locked = attemptService.recordFailure(user, "192.168.1.1");

        // Then
        assertTrue(locked);
        verify(attemptRepository).save(any(AuthenticationAttempt.class));
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.isLocked());
        assertNotNull(savedUser.getLockedUntil());
    }

    @Test
    void recordFailure_SetsLockoutDurationTo30Minutes() {
        // Given
        User user = createTestUser("test@example.com");
        LocalDateTime before = LocalDateTime.now();
        when(attemptRepository.findRecentFailedAttempts(eq(user.getEmail()), any(LocalDateTime.class)))
            .thenReturn(createFailedAttempts(5));

        // When
        attemptService.recordFailure(user, "192.168.1.1");

        // Then
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        LocalDateTime expectedMax = before.plusMinutes(30).plusSeconds(5); // Allow slight tolerance
        LocalDateTime expectedMin = before.plusMinutes(30).minusSeconds(5);
        assertTrue(savedUser.getLockedUntil().isAfter(expectedMin));
        assertTrue(savedUser.getLockedUntil().isBefore(expectedMax));
    }

    @Test
    void recordFailure_RecordsFailureAttempt() {
        // Given
        User user = createTestUser("test@example.com");
        when(attemptRepository.findRecentFailedAttempts(eq(user.getEmail()), any(LocalDateTime.class)))
            .thenReturn(createFailedAttempts(2));

        // When
        attemptService.recordFailure(user, "192.168.1.1");

        // Then
        verify(attemptRepository).save(attemptCaptor.capture());
        AuthenticationAttempt saved = attemptCaptor.getValue();
        assertEquals(user.getEmail(), saved.getEmail());
        assertFalse(saved.isSuccess());
    }

    @Test
    void recordFailure_RecordsIpAddress() {
        // Given
        User user = createTestUser("test@example.com");
        String ipAddress = "192.168.1.100";
        when(attemptRepository.findRecentFailedAttempts(eq(user.getEmail()), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // When
        attemptService.recordFailure(user, ipAddress);

        // Then
        verify(attemptRepository).save(attemptCaptor.capture());
        assertEquals(ipAddress, attemptCaptor.getValue().getIpAddress());
    }

    @Test
    void recordFailure_ChecksAttemptsWithinTimeWindow() {
        // Given
        User user = createTestUser("test@example.com");
        when(attemptRepository.findRecentFailedAttempts(eq(user.getEmail()), any(LocalDateTime.class)))
            .thenReturn(createFailedAttempts(3));

        // When
        attemptService.recordFailure(user, "192.168.1.1");

        // Then
        verify(attemptRepository).findRecentFailedAttempts(eq(user.getEmail()), argThat(time ->
            time.isBefore(LocalDateTime.now()) && time.isAfter(LocalDateTime.now().minusMinutes(16))
        ));
    }

    @Test
    void recordFailure_AtExactlyMaxAttempts_LocksAccount() {
        // Given
        User user = createTestUser("test@example.com");
        when(attemptRepository.findRecentFailedAttempts(eq(user.getEmail()), any(LocalDateTime.class)))
            .thenReturn(createFailedAttempts(5)); // Exactly 5

        // When
        boolean locked = attemptService.recordFailure(user, "192.168.1.1");

        // Then
        assertTrue(locked);
    }

    @Test
    void recordFailure_WithOneMoreThanMax_LocksAccount() {
        // Given
        User user = createTestUser("test@example.com");
        when(attemptRepository.findRecentFailedAttempts(eq(user.getEmail()), any(LocalDateTime.class)))
            .thenReturn(createFailedAttempts(6)); // More than max

        // When
        boolean locked = attemptService.recordFailure(user, "192.168.1.1");

        // Then
        assertTrue(locked);
    }

    private List<AuthenticationAttempt> createFailedAttempts(int count) {
        List<AuthenticationAttempt> attempts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AuthenticationAttempt attempt = new AuthenticationAttempt();
            attempt.setEmail("test@example.com");
            attempt.setSuccess(false);
            attempt.setIpAddress("192.168.1.1");
            attempts.add(attempt);
        }
        return attempts;
    }
}
