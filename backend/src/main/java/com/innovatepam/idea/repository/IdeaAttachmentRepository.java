package com.innovatepam.idea.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.innovatepam.idea.model.IdeaAttachment;

@Repository
public interface IdeaAttachmentRepository extends JpaRepository<IdeaAttachment, Long> {
    Optional<IdeaAttachment> findByIdeaId(Long ideaId);
}
