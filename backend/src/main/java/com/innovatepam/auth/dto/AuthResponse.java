package com.innovatepam.auth.dto;

import java.util.UUID;

public record AuthResponse(
    String token,
    String email,
    String role,
    UUID userId,
    long expiresIn
) {}
