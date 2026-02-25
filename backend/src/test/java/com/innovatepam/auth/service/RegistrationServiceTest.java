package com.innovatepam.auth.service;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.innovatepam.auth.dto.AuthResponse;
import com.innovatepam.auth.dto.RegisterRequest;
import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.UserRepository;
import com.innovatepam.auth.security.JwtService;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
            userRepository,
            roleService,
            passwordEncoder,
            jwtService
        );
    }

    @Test
    void register_WithValidRequest_CreatesUserAndReturnsAuthResponse() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Password123",
            "submitter"
        );

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("submitter");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setRole(role);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleService.getRoleByName("submitter")).thenReturn(role);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        AuthResponse response = registrationService.register(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("test@example.com", response.email());
        assertEquals("submitter", response.role());
        // Verify userId is present and is a valid UUID
        assertNotNull(response.userId());
        assertEquals(86400L, response.expiresIn());

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("test@example.com", capturedUser.getEmail());
        assertEquals("hashed-password", capturedUser.getPasswordHash());
        assertEquals(role, capturedUser.getRole());
        assertFalse(capturedUser.isLocked());
    }

    @Test
    void register_WithDuplicateEmail_ThrowsConflictException() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "existing@example.com",
            "Password123",
            "submitter"
        );

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When/Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> registrationService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Email already registered"));

        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void register_NormalizesEmailToLowercase() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "Test@Example.COM",
            "Password123",
            "submitter"
        );

        Role role = new Role();
        role.setName("submitter");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setRole(role);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleService.getRoleByName("submitter")).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        registrationService.register(request);

        // Then
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(userCaptor.capture());
        assertEquals("test@example.com", userCaptor.getValue().getEmail());
    }

    @Test
    void register_TrimsEmailWhitespace() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "  test@example.com  ",
            "Password123",
            "submitter"
        );

        Role role = new Role();
        role.setName("submitter");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setRole(role);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleService.getRoleByName("submitter")).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        registrationService.register(request);

        // Then
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(userCaptor.capture());
        assertEquals("test@example.com", userCaptor.getValue().getEmail());
    }

    @Test
    void register_TrimsPasswordWhitespace() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "  Password123  ",
            "submitter"
        );

        Role role = new Role();
        role.setName("submitter");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setRole(role);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleService.getRoleByName("submitter")).thenReturn(role);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        registrationService.register(request);

        // Then
        verify(passwordEncoder).encode("Password123");
    }

    @Test
    void register_HashePasswordBeforeSaving() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "PlainPassword",
            "submitter"
        );

        Role role = new Role();
        role.setName("submitter");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setRole(role);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleService.getRoleByName("submitter")).thenReturn(role);
        when(passwordEncoder.encode("PlainPassword")).thenReturn("$2a$12$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        registrationService.register(request);

        // Then
        verify(passwordEncoder).encode("PlainPassword");
        verify(userRepository).save(userCaptor.capture());
        assertEquals("$2a$12$hashedpassword", userCaptor.getValue().getPasswordHash());
    }

    @Test
    void register_SetsUserAsNotLocked() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Password123",
            "submitter"
        );

        Role role = new Role();
        role.setName("submitter");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setRole(role);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleService.getRoleByName("submitter")).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        registrationService.register(request);

        // Then
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isLocked());
    }

    @Test
    void register_WithEvaluatorAdminRole_CreatesUserWithCorrectRole() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "admin@example.com",
            "Password123",
            "evaluator/admin"
        );

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("evaluator/admin");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("admin@example.com");
        savedUser.setRole(role);

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(roleService.getRoleByName("evaluator/admin")).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        // When
        AuthResponse response = registrationService.register(request);

        // Then
        assertEquals("evaluator/admin", response.role());
        verify(roleService).getRoleByName("evaluator/admin");
    }
}
