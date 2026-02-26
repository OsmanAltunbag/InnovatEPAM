package com.innovatepam.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.innovatepam.auth.dto.AuthResponse;
import com.innovatepam.auth.dto.LoginRequest;
import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.UserRepository;
import com.innovatepam.auth.security.JwtService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationAttemptService attemptService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userRepository,
            passwordEncoder,
            jwtService,
            attemptService
        );
    }

    private User createTestUser(String email) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("submitter");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole(role);
        user.setLocked(false);
        return user;
    }

    @Test
    void login_WithValidCredentials_ReturnsAuthResponse() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("test@example.com", "Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        AuthResponse response = authService.login(request, "192.168.1.1");

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("test@example.com", response.email());
        assertEquals(86400L, response.expiresIn());
    }

    @Test
    void login_WithNonExistentUser_ThrowsUnauthorizedException() {
        // Given
        LoginRequest request = new LoginRequest("unknown@example.com", "Password123");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When/Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login(request, "192.168.1.1"));
        assertTrue(ex.getReason().contains("Invalid email or password"));
    }

    @Test
    void login_WithIncorrectPassword_ThrowsUnauthorizedException() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", user.getPasswordHash())).thenReturn(false);
        when(attemptService.recordFailure(user, "192.168.1.1")).thenReturn(false);

        // When/Then
        assertThrows(ResponseStatusException.class, () -> authService.login(request, "192.168.1.1"));
    }

    @Test
    void login_WithIncorrectPassword_RecordsFailureAttempt() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", user.getPasswordHash())).thenReturn(false);
        when(attemptService.recordFailure(user, "192.168.1.1")).thenReturn(false);

        // When
        try {
            authService.login(request, "192.168.1.1");
        } catch (ResponseStatusException e) {
            // Expected
        }

        // Then
        verify(attemptService).recordFailure(user, "192.168.1.1");
    }

    @Test
    void login_WithLockedAccount_ThrowsForbiddenException() {
        // Given
        User user = createTestUser("test@example.com");
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        LoginRequest request = new LoginRequest("test@example.com", "Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When/Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login(request, "192.168.1.1"));
        assertTrue(ex.getReason().contains("Account locked"));
    }

    @Test
    void login_WithCorrectPassword_UnlocksAccount() {
        // Given
        User user = createTestUser("test@example.com");
        user.setLocked(false); // Initially not locked
        LoginRequest request = new LoginRequest("test@example.com", "Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        authService.login(request, "192.168.1.1");

        // Then
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertFalse(savedUser.isLocked());
        assertNull(savedUser.getLockedUntil());
    }

    @Test
    void login_NormalizesEmailToLowercase() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("Test@Example.COM", "Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        authService.login(request, "192.168.1.1");

        // Then
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_TrimsEmailWhitespace() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("  test@example.com  ", "Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        authService.login(request, "192.168.1.1");

        // Then
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_WithAccountLockoutDueToFailures_ThrowsForbiddenException() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", user.getPasswordHash())).thenReturn(false);
        when(attemptService.recordFailure(user, "192.168.1.1")).thenReturn(true); // Account gets locked

        // When/Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login(request, "192.168.1.1"));
        assertTrue(ex.getReason().contains("Account locked"));
    }

    @Test
    void login_RecordsSuccessfulLogin() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("test@example.com", "Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        authService.login(request, "192.168.1.1");

        // Then
        verify(attemptService).recordSuccess(user.getEmail(), "192.168.1.1");
    }

    @Test
    void login_GeneratesValidJwtToken() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("test@example.com", "Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("valid-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        AuthResponse response = authService.login(request, "192.168.1.1");

        // Then
        assertEquals("valid-jwt-token", response.token());
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_IncludesExpirationInResponse() {
        // Given
        User user = createTestUser("test@example.com");
        LoginRequest request = new LoginRequest("test@example.com", "Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        // When
        AuthResponse response = authService.login(request, "192.168.1.1");

        // Then
        assertEquals(3600L, response.expiresIn());
    }
}
