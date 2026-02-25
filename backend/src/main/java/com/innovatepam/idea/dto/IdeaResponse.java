package com.innovatepam.idea.dto;

import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import java.time.LocalDateTime;

public record IdeaResponse(
    Long id,
    String title,
    String category,
    IdeaStatus status,
    String submitterName,
    LocalDateTime createdAt,
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
            idea.getCreatedAt(),
            idea.getAttachment() != null,
            idea.getEvaluations() != null ? idea.getEvaluations().size() : 0
        );
    }
}
