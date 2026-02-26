package com.innovatepam.auth.dto;

public record ErrorResponse(
    int status,
    String message,
    String timestamp,
    String path
) {}
