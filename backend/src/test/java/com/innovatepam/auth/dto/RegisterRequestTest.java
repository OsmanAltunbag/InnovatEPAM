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

class RegisterRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequest_WithValidData_IsValid() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Password123",
            "submitter"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_WithInvalidEmail_HasViolation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "invalid-email",
            "Password123",
            "submitter"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Invalid email format")));
    }

    @Test
    void registerRequest_WithEmptyEmail_HasViolation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "",
            "Password123",
            "submitter"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void registerRequest_WithShortPassword_HasViolation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Pass123",
            "submitter"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("at least 8 characters")));
    }

    @Test
    void registerRequest_WithEmptyPassword_HasViolation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "",
            "submitter"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void registerRequest_WithInvalidRole_HasViolation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Password123",
            "invalid_role"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("submitter") || v.getMessage().contains("evaluator/admin")));
    }

    @Test
    void registerRequest_WithEmptyRole_HasViolation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Password123",
            ""
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void registerRequest_WithSubmitterRole_IsValid() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Password123",
            "submitter"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_WithEvaluatorAdminRole_IsValid() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Password123",
            "evaluator/admin"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_WithMinimumPasswordLength_IsValid() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "Pass1234",  // 8 characters
            "submitter"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_WithLongPassword_IsValid() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "VeryLongPasswordWith32Characters1234567890",
            "submitter"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_WithValidEmailFormats_IsValid() {
        // Given - various valid email formats
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "first.last@subdomain.example.com"
        };

        // When/Then
        for (String email : validEmails) {
            RegisterRequest request = new RegisterRequest(email, "Password123", "submitter");
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Email should be valid: " + email);
        }
    }
}
