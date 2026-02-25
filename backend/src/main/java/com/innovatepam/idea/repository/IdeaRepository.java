package com.innovatepam.idea.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;

@Repository
public interface IdeaRepository extends JpaRepository<Idea, Long> {
    Page<Idea> findByStatus(IdeaStatus status, Pageable pageable);

    List<Idea> findByStatus(IdeaStatus status);

    Page<Idea> findBySubmitterId(UUID submitterId, Pageable pageable);

    List<Idea> findBySubmitterId(UUID submitterId);

    Page<Idea> findByCategory(String category, Pageable pageable);

    Page<Idea> findByCategoryAndStatus(String category, IdeaStatus status, Pageable pageable);

    List<Idea> findByCategoryAndStatus(String category, IdeaStatus status);
}
