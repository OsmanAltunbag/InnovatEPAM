package com.innovatepam.auth.dto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class LoginRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void loginRequest_WithValidData_IsValid() {
        // Given
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "Password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_WithInvalidEmail_HasViolation() {
        // Given
        LoginRequest request = new LoginRequest(
            "invalid-email",
            "Password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Invalid email format")));
    }

    @Test
    void loginRequest_WithEmptyEmail_HasViolation() {
        // Given
        LoginRequest request = new LoginRequest(
            "",
            "Password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void loginRequest_WithEmptyPassword_HasViolation() {
        // Given
        LoginRequest request = new LoginRequest(
            "test@example.com",
            ""
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void loginRequest_WithValidEmail_IsValid() {
        // Given
        LoginRequest request = new LoginRequest(
            "user@example.com",
            "AnyPassword"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_WithShortPassword_IsValid() {
        // Given - LoginRequest doesn't validate password length
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "short"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_WithLongPassword_IsValid() {
        // Given
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "VeryLongPasswordThatDoesNotNeedToMeetMinimumLengthRequirements123456789"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_WithSpecialCharactersInPassword_IsValid() {
        // Given
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "P@ssw0rd!#$%^&*()"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_WithValidEmailFormats_IsValid() {
        // Given - various valid email formats
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk"
        };

        // When/Then
        for (String email : validEmails) {
            LoginRequest request = new LoginRequest(email, "Password");
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Email should be valid: " + email);
        }
    }

    @Test
    void loginRequest_WithWhitespaceInEmail_IsInvalid() {
        // Given
        LoginRequest request = new LoginRequest(
            "test @example.com",
            "Password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void loginRequest_WithoutAtSignInEmail_IsInvalid() {
        // Given
        LoginRequest request = new LoginRequest(
            "testexample.com",
            "Password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void loginRequest_WithoutDomainInEmail_IsInvalid() {
        // Given
        LoginRequest request = new LoginRequest(
            "test@",
            "Password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void loginRequest_WithNumericPassword_IsValid() {
        // Given
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "12345678"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }
}
