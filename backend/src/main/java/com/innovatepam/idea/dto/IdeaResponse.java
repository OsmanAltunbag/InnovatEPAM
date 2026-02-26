package com.innovatepam.idea.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;

public record IdeaResponse(
    Long id,
    String title,
    String category,
    IdeaStatus status,
    String submitterName,
    UUID submitterId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean hasAttachment,
    int evaluationCount
) {
    public static IdeaResponse from(Idea idea) {
        return new IdeaResponse(
            idea.getId(),
            idea.getTitle(),
            idea.getCategory(),
            idea.getStatus(),
            idea.getSubmitter().getEmail(),
            idea.getSubmitter().getId(),
            idea.getCreatedAt(),
            idea.getUpdatedAt(),
            idea.getAttachment() != null,
            idea.getEvaluations() != null ? idea.getEvaluations().size() : 0
        );
    }
}
