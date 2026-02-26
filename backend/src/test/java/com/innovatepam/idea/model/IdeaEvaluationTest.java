package com.innovatepam.idea.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;

class IdeaEvaluationTest {
    
    private User submitter;
    private User evaluator;
    private Idea idea;
    private IdeaEvaluation evaluation;
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
        
        submitter = new User();
        submitter.setId(UUID.randomUUID());
        submitter.setEmail("submitter@example.com");
        submitter.setPasswordHash("hashed_password");
        submitter.setRole(submitterRole);
        submitter.setCreatedAt(LocalDateTime.now());
        
        evaluator = new User();
        evaluator.setId(UUID.randomUUID());
        evaluator.setEmail("evaluator@example.com");
        evaluator.setPasswordHash("hashed_password");
        evaluator.setRole(evaluatorRole);
        evaluator.setCreatedAt(LocalDateTime.now());
        
        idea = new Idea();
        idea.setTitle("Test Idea");
        idea.setDescription("Test description");
        idea.setCategory("Process Improvement");
        idea.setSubmitter(submitter);
        idea.onCreate();
        
        evaluation = new IdeaEvaluation();
        evaluation.setIdea(idea);
        evaluation.setEvaluator(evaluator);
    }
    
    @Test
    void testOnCreateSetsTimestamp() {
        evaluation.setComment("Initial review");
        evaluation.onCreate();
        
        assertNotNull(evaluation.getCreatedAt(), "Created timestamp should be set");
    }
    
    @Test
    void testEvaluationWithStatusSnapshot() {
        evaluation.setComment("Moving to Under Review for detailed analysis");
        evaluation.setStatusSnapshot(IdeaStatus.UNDER_REVIEW);
        evaluation.onCreate();
        
        assertEquals("Moving to Under Review for detailed analysis", evaluation.getComment());
        assertEquals(IdeaStatus.UNDER_REVIEW, evaluation.getStatusSnapshot());
        assertNotNull(evaluation.getCreatedAt());
    }
    
    @Test
    void testEvaluationWithoutStatusSnapshot() {
        evaluation.setComment("Just a comment without status change");
        evaluation.setStatusSnapshot(null);
        evaluation.onCreate();
        
        assertEquals("Just a comment without status change", evaluation.getComment());
        assertNull(evaluation.getStatusSnapshot(), "Status snapshot can be null for comments");
    }
    
    @Test
    void testEvaluatorRelationship() {
        evaluation.setComment("Test comment");
        evaluation.onCreate();
        
        assertEquals(evaluator, evaluation.getEvaluator());
        assertEquals("evaluator@example.com", evaluation.getEvaluator().getEmail());
        assertEquals("EVALUATOR", evaluation.getEvaluator().getRole().getName());
    }
    
    @Test
    void testIdeaRelationship() {
        evaluation.setComment("Test comment");
        evaluation.onCreate();
        
        assertEquals(idea, evaluation.getIdea());
        assertEquals("Test Idea", evaluation.getIdea().getTitle());
    }
    
    @Test
    void testRejectionEvaluation() {
        evaluation.setComment("Rejected due to insufficient detail");
        evaluation.setStatusSnapshot(IdeaStatus.REJECTED);
        evaluation.onCreate();
        
        assertEquals(IdeaStatus.REJECTED, evaluation.getStatusSnapshot());
        assertTrue(evaluation.getComment().contains("Rejected"), "Comment should explain rejection");
    }
    
    @Test
    void testAcceptanceEvaluation() {
        evaluation.setComment("Accepted for implementation in Q3");
        evaluation.setStatusSnapshot(IdeaStatus.ACCEPTED);
        evaluation.onCreate();
        
        assertEquals(IdeaStatus.ACCEPTED, evaluation.getStatusSnapshot());
        assertTrue(evaluation.getComment().contains("Accepted"), "Comment should indicate acceptance");
    }
    
    @Test
    void testMultipleEvaluationsChronology() throws InterruptedException {
        IdeaEvaluation eval1 = new IdeaEvaluation();
        eval1.setIdea(idea);
        eval1.setEvaluator(evaluator);
        eval1.setComment("First comment");
        eval1.setStatusSnapshot(IdeaStatus.UNDER_REVIEW);
        eval1.onCreate();
        
        Thread.sleep(10);
        
        IdeaEvaluation eval2 = new IdeaEvaluation();
        eval2.setIdea(idea);
        eval2.setEvaluator(evaluator);
        eval2.setComment("Second comment");
        eval2.setStatusSnapshot(null);
        eval2.onCreate();
        
        assertTrue(eval2.getCreatedAt().isAfter(eval1.getCreatedAt()),
            "Second evaluation should have later timestamp");
    }
    
    @Test
    void testCommentMaxLength() {
        String longComment = "A".repeat(5000);
        evaluation.setComment(longComment);
        evaluation.onCreate();
        
        assertEquals(5000, evaluation.getComment().length(), "Should support long comments");
    }
    
    @Test
    void testAllStatusSnapshots() {
        // Test all possible status snapshot values
        evaluation.setComment("Under Review");
        evaluation.setStatusSnapshot(IdeaStatus.UNDER_REVIEW);
        evaluation.onCreate();
        assertEquals(IdeaStatus.UNDER_REVIEW, evaluation.getStatusSnapshot());
        
        IdeaEvaluation eval2 = new IdeaEvaluation();
        eval2.setIdea(idea);
        eval2.setEvaluator(evaluator);
        eval2.setComment("Accepted");
        eval2.setStatusSnapshot(IdeaStatus.ACCEPTED);
        eval2.onCreate();
        assertEquals(IdeaStatus.ACCEPTED, eval2.getStatusSnapshot());
        
        IdeaEvaluation eval3 = new IdeaEvaluation();
        eval3.setIdea(idea);
        eval3.setEvaluator(evaluator);
        eval3.setComment("Rejected");
        eval3.setStatusSnapshot(IdeaStatus.REJECTED);
        eval3.onCreate();
        assertEquals(IdeaStatus.REJECTED, eval3.getStatusSnapshot());
    }
}
