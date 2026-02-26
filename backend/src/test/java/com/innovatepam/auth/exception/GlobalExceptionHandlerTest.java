package com.innovatepam.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import com.innovatepam.auth.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException methodArgException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private FieldError fieldError;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");
    }

    @Test
    void handleValidation_WithFieldError_Returns400WithMessage() {
        // Given
        when(methodArgException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);
        when(fieldError.getDefaultMessage()).thenReturn("Email is invalid");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidation(methodArgException, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Email is invalid", response.getBody().message());
    }

    @Test
    void handleValidation_WithoutFieldError_UsesDefaultMessage() {
        // Given
        when(methodArgException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(null);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidation(methodArgException, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", response.getBody().message());
    }

    @Test
    void handleValidation_IncludesRequestPath() {
        // Given
        when(methodArgException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);
        when(fieldError.getDefaultMessage()).thenReturn("Error message");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidation(methodArgException, request);

        // Then
        assertEquals("/api/v1/auth/register", response.getBody().path());
    }

    @Test
    void handleResponseStatus_WithUnauthorized_Returns401() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password"
        );

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().status());
        assertEquals("Invalid email or password", response.getBody().message());
    }

    @Test
    void handleResponseStatus_WithForbidden_Returns403() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Account locked"
        );

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().status());
    }

    @Test
    void handleResponseStatus_WithBadRequest_Returns400() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid input"
        );

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
    }

    @Test
    void handleResponseStatus_WithNullReason_UsesStatusPhrase() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody().message());
    }

    @Test
    void handleUnexpected_WithGenericException_Returns500() {
        // Given
        Exception ex = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Unexpected server error", response.getBody().message());
    }

    @Test
    void handleUnexpected_WithAnyException_Returns500() {
        // Given
        Exception ex = new RuntimeException("Runtime error");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
    }

    @Test
    void errorResponse_IncludesTimestamp() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Error"
        );

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        // Then
        assertNotNull(response.getBody().timestamp());
        assertFalse(response.getBody().timestamp().isEmpty());
    }

    @Test
    void errorResponse_IncludesAllRequiredFields() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed"
        );

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        // Then
        ErrorResponse body = response.getBody();
        assertNotNull(body.status());
        assertNotNull(body.message());
        assertNotNull(body.timestamp());
        assertNotNull(body.path());
    }

    @Test
    void handleValidation_WithConflictStatus_ReturnsConflict() {
        // Note: This tests that different status codes are handled properly
        // Given
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Email already exists"
        );

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
    }
}
