package com.innovatepam.idea.dto;

import com.innovatepam.idea.model.IdeaEvaluation;
import com.innovatepam.idea.model.IdeaStatus;
import java.time.LocalDateTime;

public record IdeaEvaluationResponse(
    Long id,
    String evaluatorName,
    String comment,
    IdeaStatus statusSnapshot,
    LocalDateTime createdAt
) {
    public static IdeaEvaluationResponse from(IdeaEvaluation evaluation) {
        return new IdeaEvaluationResponse(
            evaluation.getId(),
            evaluation.getEvaluator().getEmail(),
            evaluation.getComment(),
            evaluation.getStatusSnapshot(),
            evaluation.getCreatedAt()
        );
    }
}
