package com.innovatepam.idea.service;

import com.innovatepam.auth.model.User;
import com.innovatepam.idea.exception.IdeaNotFoundException;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaEvaluation;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaEvaluationRepository;
import com.innovatepam.idea.repository.IdeaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdeaEvaluationService {
    private final IdeaRepository ideaRepository;
    private final IdeaEvaluationRepository ideaEvaluationRepository;

    public IdeaEvaluationService(
        IdeaRepository ideaRepository,
        IdeaEvaluationRepository ideaEvaluationRepository
    ) {
        this.ideaRepository = ideaRepository;
        this.ideaEvaluationRepository = ideaEvaluationRepository;
    }

    @Transactional
    public IdeaEvaluation addComment(Long ideaId, User evaluator, String comment) {
        Idea idea = ideaRepository.findById(ideaId)
            .orElseThrow(() -> new IdeaNotFoundException(ideaId));

        return saveEvaluation(idea, evaluator, comment, null);
    }

    @Transactional(readOnly = true)
    public List<IdeaEvaluation> getEvaluationHistory(Long ideaId) {
        if (!ideaRepository.existsById(ideaId)) {
            throw new IdeaNotFoundException(ideaId);
        }
        return ideaEvaluationRepository.findByIdeaIdOrderByCreatedAtAsc(ideaId);
    }

    @Transactional
    public IdeaEvaluation addStatusEvaluation(
        Idea idea,
        User evaluator,
        String comment,
        IdeaStatus statusSnapshot
    ) {
        return saveEvaluation(idea, evaluator, comment, statusSnapshot);
    }

    private IdeaEvaluation saveEvaluation(
        Idea idea,
        User evaluator,
        String comment,
        IdeaStatus statusSnapshot
    ) {
        IdeaEvaluation evaluation = new IdeaEvaluation();
        evaluation.setIdea(idea);
        evaluation.setEvaluator(evaluator);
        evaluation.setComment(comment);
        evaluation.setStatusSnapshot(statusSnapshot);
        return ideaEvaluationRepository.save(evaluation);
    }
}
