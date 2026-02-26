package com.innovatepam.idea.exception;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.innovatepam.auth.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(basePackages = "com.innovatepam.idea")
public class IdeaExceptionHandler {
    
    @ExceptionHandler(IdeaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIdeaNotFound(IdeaNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<InvalidStatusTransitionErrorResponse> handleInvalidStatusTransition(
        InvalidStatusTransitionException ex, 
        HttpServletRequest request
    ) {
        InvalidStatusTransitionErrorResponse response = new InvalidStatusTransitionErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            OffsetDateTime.now().toString(),
            request.getRequestURI(),
            ex.getCurrentStatus() != null ? ex.getCurrentStatus().name() : null,
            ex.getTargetStatus() != null ? ex.getTargetStatus().name() : null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the maximum allowed limit", request.getRequestURI());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return buildResponse(
            HttpStatus.CONFLICT, 
            "Idea was modified by another user. Please refresh and try again.", 
            request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            OffsetDateTime.now().toString(),
            request.getRequestURI(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, String path) {
        ErrorResponse response = new ErrorResponse(
            status.value(),
            message,
            OffsetDateTime.now().toString(),
            path
        );
        return ResponseEntity.status(status).body(response);
    }

    public record InvalidStatusTransitionErrorResponse(
        int status,
        String message,
        String timestamp,
        String path,
        String currentStatus,
        String attemptedStatus
    ) {}

    public record ValidationErrorResponse(
        int status,
        String message,
        String timestamp,
        String path,
        Map<String, String> details
    ) {}
}
