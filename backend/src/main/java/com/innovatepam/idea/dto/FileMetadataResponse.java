package com.innovatepam.idea.dto;

import com.innovatepam.idea.model.IdeaAttachment;
import java.time.LocalDateTime;

public record FileMetadataResponse(
    Long id,
    String originalFilename,
    long fileSize,
    LocalDateTime createdAt
) {
    public static FileMetadataResponse from(IdeaAttachment attachment) {
        return new FileMetadataResponse(
            attachment.getId(),
            attachment.getOriginalFilename(),
            attachment.getFileSize(),
            attachment.getCreatedAt()
        );
    }
}
