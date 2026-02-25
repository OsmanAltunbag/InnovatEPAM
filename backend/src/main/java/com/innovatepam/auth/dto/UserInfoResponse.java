package com.innovatepam.auth.dto;

public record UserInfoResponse(
    String userId,
    String email,
    String role,
    String createdAt
) {}
