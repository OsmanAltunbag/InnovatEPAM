package com.innovatepam.idea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(
    @NotBlank(message = "Comment is required")
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    String comment
) {}
