package com.innovatepam.idea.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.innovatepam.idea.model.IdeaEvaluation;
import com.innovatepam.idea.model.IdeaStatus;

public record IdeaEvaluationResponse(
    Long id,
    Long ideaId,
    String evaluatorName,
    UUID evaluatorId,
    String comment,
    IdeaStatus statusSnapshot,
    LocalDateTime createdAt
) {
    public static IdeaEvaluationResponse from(IdeaEvaluation evaluation) {
        return new IdeaEvaluationResponse(
            evaluation.getId(),
            evaluation.getIdea().getId(),
            evaluation.getEvaluator().getEmail(),
            evaluation.getEvaluator().getId(),
            evaluation.getComment(),
            evaluation.getStatusSnapshot(),
            evaluation.getCreatedAt()
        );
    }
}
