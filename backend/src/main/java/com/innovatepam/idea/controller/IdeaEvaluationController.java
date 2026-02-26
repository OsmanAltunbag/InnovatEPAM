package com.innovatepam.idea.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.UserRepository;
import com.innovatepam.idea.dto.AddCommentRequest;
import com.innovatepam.idea.dto.EvaluationHistoryResponse;
import com.innovatepam.idea.dto.IdeaEvaluationResponse;
import com.innovatepam.idea.dto.IdeaResponse;
import com.innovatepam.idea.dto.UpdateIdeaStatusRequest;
import com.innovatepam.idea.exception.UnauthorizedAccessException;
import com.innovatepam.idea.model.IdeaEvaluation;
import com.innovatepam.idea.service.IdeaEvaluationService;
import com.innovatepam.idea.service.IdeaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ideas")
public class IdeaEvaluationController {
    private final IdeaService ideaService;
    private final IdeaEvaluationService evaluationService;
    private final UserRepository userRepository;

    public IdeaEvaluationController(
        IdeaService ideaService,
        IdeaEvaluationService evaluationService,
        UserRepository userRepository
    ) {
        this.ideaService = ideaService;
        this.evaluationService = evaluationService;
        this.userRepository = userRepository;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EVALUATOR', 'ADMIN')")
    public ResponseEntity<IdeaResponse> updateIdeaStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateIdeaStatusRequest request,
        Authentication authentication
    ) {
        User evaluator = getCurrentUser(authentication);
        IdeaResponse response = ideaService.updateStatus(id, request.newStatus(), evaluator, request.comment());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('EVALUATOR', 'ADMIN')")
    public ResponseEntity<IdeaEvaluationResponse> addComment(
        @PathVariable Long id,
        @Valid @RequestBody AddCommentRequest request,
        Authentication authentication
    ) {
        User evaluator = getCurrentUser(authentication);
        IdeaEvaluation evaluation = evaluationService.addComment(id, evaluator, request.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(IdeaEvaluationResponse.from(evaluation));
    }

    @GetMapping("/{id}/evaluations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EvaluationHistoryResponse> getEvaluationHistory(@PathVariable Long id) {
        List<IdeaEvaluation> evaluations = evaluationService.getEvaluationHistory(id);
        List<IdeaEvaluationResponse> responses = evaluations.stream()
            .map(IdeaEvaluationResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(new EvaluationHistoryResponse(id, responses));
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedAccessException("User not found"));
    }
}
