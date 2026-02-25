package com.innovatepam.idea.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.innovatepam.idea.model.IdeaEvaluation;

@Repository
public interface IdeaEvaluationRepository extends JpaRepository<IdeaEvaluation, Long> {
    List<IdeaEvaluation> findByIdeaIdOrderByCreatedAtAsc(Long ideaId);

    List<IdeaEvaluation> findByEvaluatorId(UUID evaluatorId);
}
