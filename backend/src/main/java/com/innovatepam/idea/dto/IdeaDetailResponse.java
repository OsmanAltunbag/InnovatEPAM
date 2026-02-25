package com.innovatepam.idea.dto;

import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record IdeaDetailResponse(
    Long id,
    String title,
    String description,
    String category,
    IdeaStatus status,
    String submitterName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    FileMetadataResponse attachment,
    List<IdeaEvaluationResponse> evaluations
) {
    public static IdeaDetailResponse from(Idea idea) {
        return new IdeaDetailResponse(
            idea.getId(),
            idea.getTitle(),
            idea.getDescription(),
            idea.getCategory(),
            idea.getStatus(),
            idea.getSubmitter().getEmail(),
            idea.getCreatedAt(),
            idea.getUpdatedAt(),
            idea.getAttachment() != null ? FileMetadataResponse.from(idea.getAttachment()) : null,
            idea.getEvaluations() != null 
                ? idea.getEvaluations().stream()
                    .map(IdeaEvaluationResponse::from)
                    .collect(Collectors.toList())
                : List.of()
        );
    }
}
