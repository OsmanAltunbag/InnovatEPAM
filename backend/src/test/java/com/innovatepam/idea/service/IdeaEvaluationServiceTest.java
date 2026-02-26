package com.innovatepam.idea.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;
import com.innovatepam.idea.exception.IdeaNotFoundException;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaEvaluation;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaEvaluationRepository;
import com.innovatepam.idea.repository.IdeaRepository;

@ExtendWith(MockitoExtension.class)
class IdeaEvaluationServiceTest {

    @Mock
    private IdeaEvaluationRepository evaluationRepository;

    @Mock
    private IdeaRepository ideaRepository;

    @InjectMocks
    private IdeaEvaluationService evaluationService;

    private User evaluator;
    private User submitter;
    private Idea idea;
    private Role submitterRole;
    private Role evaluatorRole;

    @BeforeEach
    void setUp() {
        submitterRole = new Role();
        submitterRole.setId(UUID.randomUUID());
        submitterRole.setName("SUBMITTER");
        submitterRole.setCreatedAt(LocalDateTime.now());

        evaluatorRole = new Role();
        evaluatorRole.setId(UUID.randomUUID());
        evaluatorRole.setName("EVALUATOR");
        evaluatorRole.setCreatedAt(LocalDateTime.now());

        evaluator = new User();
        evaluator.setId(UUID.randomUUID());
        evaluator.setEmail("evaluator@example.com");
        evaluator.setPasswordHash("hashed_password");
        evaluator.setRole(evaluatorRole);
        evaluator.setCreatedAt(LocalDateTime.now());

        submitter = new User();
        submitter.setId(UUID.randomUUID());
        submitter.setEmail("submitter@example.com");
        submitter.setPasswordHash("hashed_password");
        submitter.setRole(submitterRole);
        submitter.setCreatedAt(LocalDateTime.now());

        idea = new Idea();
        idea.setId(1L);
        idea.setTitle("Test Idea");
        idea.setDescription("Description");
        idea.setCategory("Process Improvement");
        idea.setSubmitter(submitter);
        idea.setStatus(IdeaStatus.SUBMITTED);
        idea.onCreate();
    }

    @Test
    void testAddCommentSuccess() {
        when(ideaRepository.findById(1L)).thenReturn(Optional.of(idea));
        when(evaluationRepository.save(any(IdeaEvaluation.class))).thenAnswer(invocation -> {
            IdeaEvaluation eval = invocation.getArgument(0);
            eval.setId(1L);
            return eval;
        });

    IdeaEvaluation response = evaluationService.addComment(1L, evaluator, "Great idea, moving to review");

    assertNotNull(response);
    assertEquals("Great idea, moving to review", response.getComment());
    assertEquals("evaluator@example.com", response.getEvaluator().getEmail());

        ArgumentCaptor<IdeaEvaluation> captor = ArgumentCaptor.forClass(IdeaEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        IdeaEvaluation saved = captor.getValue();
        assertEquals("Great idea, moving to review", saved.getComment());
        assertNull(saved.getStatusSnapshot());
    }

    @Test
    void testAddCommentIdeaNotFound() {
        when(ideaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IdeaNotFoundException.class, () -> {
            evaluationService.addComment(99L, evaluator, "Comment");
        });

        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void testGetEvaluationHistory() {
        IdeaEvaluation eval1 = new IdeaEvaluation();
        eval1.setId(1L);
        eval1.setIdea(idea);
        eval1.setEvaluator(evaluator);
        eval1.setComment("First comment");
        eval1.setStatusSnapshot(IdeaStatus.UNDER_REVIEW);
        eval1.onCreate();

        IdeaEvaluation eval2 = new IdeaEvaluation();
        eval2.setId(2L);
        eval2.setIdea(idea);
        eval2.setEvaluator(evaluator);
        eval2.setComment("Second comment");
        eval2.setStatusSnapshot(null);
        eval2.onCreate();

        when(ideaRepository.existsById(1L)).thenReturn(true);
        when(evaluationRepository.findByIdeaIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList(eval1, eval2));

        List<IdeaEvaluation> history = evaluationService.getEvaluationHistory(1L);

        assertEquals(2, history.size());
        assertEquals("First comment", history.get(0).getComment());
        assertEquals(IdeaStatus.UNDER_REVIEW, history.get(0).getStatusSnapshot());
        assertEquals("Second comment", history.get(1).getComment());
        assertNull(history.get(1).getStatusSnapshot());
    }

    @Test
    void testGetEvaluationHistoryIdeaNotFound() {
        when(ideaRepository.existsById(99L)).thenReturn(false);

        assertThrows(IdeaNotFoundException.class, () -> {
            evaluationService.getEvaluationHistory(99L);
        });
    }

    @Test
    void testRecordStatusTransition() {
        when(evaluationRepository.save(any(IdeaEvaluation.class))).thenAnswer(invocation -> {
            IdeaEvaluation eval = invocation.getArgument(0);
            eval.setId(1L);
            return eval;
        });

        evaluationService.addStatusEvaluation(idea, evaluator, "Moving to review", IdeaStatus.UNDER_REVIEW);

        ArgumentCaptor<IdeaEvaluation> captor = ArgumentCaptor.forClass(IdeaEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        IdeaEvaluation saved = captor.getValue();
        
        assertEquals("Moving to review", saved.getComment());
        assertEquals(IdeaStatus.UNDER_REVIEW, saved.getStatusSnapshot());
        assertEquals(evaluator, saved.getEvaluator());
        assertEquals(idea, saved.getIdea());
    }
}
