package com.innovatepam.idea.dto;

import java.util.List;

public record EvaluationHistoryResponse(
    Long ideaId,
    List<IdeaEvaluationResponse> evaluations
) {}
