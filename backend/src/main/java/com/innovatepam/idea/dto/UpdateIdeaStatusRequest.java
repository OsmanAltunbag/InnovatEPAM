package com.innovatepam.idea.dto;

import com.innovatepam.idea.model.IdeaStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateIdeaStatusRequest(
    @NotNull(message = "New status is required")
    IdeaStatus newStatus,

    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    String comment
) {}
