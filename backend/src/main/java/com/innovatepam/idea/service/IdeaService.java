package com.innovatepam.idea.service;

import com.innovatepam.auth.model.User;
import com.innovatepam.idea.exception.IdeaNotFoundException;
import com.innovatepam.idea.exception.InvalidStatusTransitionException;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaAttachment;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IdeaService {
    private final IdeaRepository ideaRepository;
    private final IdeaEvaluationService evaluationService;
    private final FileStorageService fileStorageService;

    public IdeaService(
        IdeaRepository ideaRepository,
        IdeaEvaluationService evaluationService,
        FileStorageService fileStorageService
    ) {
        this.ideaRepository = ideaRepository;
        this.evaluationService = evaluationService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Idea createIdea(
        String title,
        String description,
        String category,
        User submitter,
        MultipartFile file
    ) {
        Idea idea = new Idea();
        idea.setTitle(title == null ? null : title.trim());
        idea.setDescription(description == null ? null : description.trim());
        idea.setCategory(category == null ? null : category.trim());
        idea.setSubmitter(submitter);
        idea.setStatus(IdeaStatus.SUBMITTED);

        Idea saved = ideaRepository.save(idea);

        if (file != null && !file.isEmpty()) {
            IdeaAttachment attachment = fileStorageService.storeFile(file, saved);
            saved.setAttachment(attachment);
            saved = ideaRepository.save(saved);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Idea getIdeaById(Long ideaId) {
        return ideaRepository.findById(ideaId)
            .orElseThrow(() -> new IdeaNotFoundException(ideaId));
    }

    @Transactional(readOnly = true)
    public Page<Idea> getIdeas(Pageable pageable) {
        return ideaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Idea> getIdeasByStatus(IdeaStatus status, Pageable pageable) {
        return ideaRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Idea> getIdeasByCategory(String category, Pageable pageable) {
        return ideaRepository.findByCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Idea> getIdeasByStatusAndCategory(IdeaStatus status, String category, Pageable pageable) {
        return ideaRepository.findByCategoryAndStatus(category, status, pageable);
    }

    @Transactional
    public Idea updateStatus(
        Long ideaId,
        IdeaStatus targetStatus,
        User evaluator,
        String comment
    ) {
        Idea idea = ideaRepository.findById(ideaId)
            .orElseThrow(() -> new IdeaNotFoundException(ideaId));

        validateStatusTransition(idea.getStatus(), targetStatus, comment);

        idea.setStatus(targetStatus);
        evaluationService.addStatusEvaluation(idea, evaluator, comment, targetStatus);

        return ideaRepository.save(idea);
    }

    private void validateStatusTransition(
        IdeaStatus currentStatus,
        IdeaStatus targetStatus,
        String comment
    ) {
        if (currentStatus == null || targetStatus == null) {
            throw new InvalidStatusTransitionException("Status is required");
        }

        if (currentStatus == IdeaStatus.SUBMITTED) {
            if (targetStatus != IdeaStatus.UNDER_REVIEW && targetStatus != IdeaStatus.REJECTED) {
                throw new InvalidStatusTransitionException(currentStatus, targetStatus);
            }
        } else if (currentStatus == IdeaStatus.UNDER_REVIEW) {
            if (targetStatus != IdeaStatus.ACCEPTED && targetStatus != IdeaStatus.REJECTED) {
                throw new InvalidStatusTransitionException(currentStatus, targetStatus);
            }
        } else {
            throw new InvalidStatusTransitionException(currentStatus, targetStatus);
        }

        if (targetStatus == IdeaStatus.REJECTED && isBlank(comment)) {
            throw new InvalidStatusTransitionException("Comment required when rejecting an idea");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
