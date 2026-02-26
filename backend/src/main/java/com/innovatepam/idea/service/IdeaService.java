package com.innovatepam.idea.service;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.innovatepam.auth.model.User;
import com.innovatepam.idea.dto.IdeaDetailResponse;
import com.innovatepam.idea.dto.IdeaResponse;
import com.innovatepam.idea.exception.IdeaNotFoundException;
import com.innovatepam.idea.exception.InvalidStatusTransitionException;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaAttachment;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaRepository;
import com.innovatepam.idea.util.IdeaStatusValidator;

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
    public IdeaResponse createIdea(
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

        return IdeaResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Idea getIdeaById(Long ideaId) {
        return ideaRepository.findById(ideaId)
            .orElseThrow(() -> new IdeaNotFoundException(ideaId));
    }

    @Transactional(readOnly = true)
    public IdeaDetailResponse getIdeaDetailById(Long ideaId) {
        Idea idea = ideaRepository.findById(ideaId)
            .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        return IdeaDetailResponse.from(idea);
    }

    @Transactional(readOnly = true)
    public Page<IdeaResponse> getIdeas(Pageable pageable) {
        return ideaRepository.findAll(pageable).map(IdeaResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<IdeaResponse> getIdeasByStatus(IdeaStatus status, Pageable pageable) {
        return ideaRepository.findByStatus(status, pageable).map(IdeaResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<IdeaResponse> getIdeasByCategory(String category, Pageable pageable) {
        return ideaRepository.findByCategory(category, pageable).map(IdeaResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<IdeaResponse> getIdeasByStatusAndCategory(IdeaStatus status, String category, Pageable pageable) {
        return ideaRepository.findByCategoryAndStatus(category, status, pageable).map(IdeaResponse::from);
    }

    @Transactional
    public IdeaResponse updateStatus(
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

        Idea saved = ideaRepository.save(idea);
        
        // Initialize lazy collections within the transaction boundary
        Hibernate.initialize(saved.getEvaluations());
        
        // Convert to DTO while still within the @Transactional boundary
        return IdeaResponse.from(saved);
    }

    private void validateStatusTransition(
        IdeaStatus currentStatus,
        IdeaStatus targetStatus,
        String comment
    ) {
        if (currentStatus == null || targetStatus == null) {
            throw new InvalidStatusTransitionException("Status is required");
        }

        if (!IdeaStatusValidator.isValidTransition(currentStatus, targetStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, targetStatus);
        }

        if (IdeaStatusValidator.isCommentRequired(targetStatus) && isBlank(comment)) {
            throw new InvalidStatusTransitionException(
                "Comment is required when accepting or rejecting an idea"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
