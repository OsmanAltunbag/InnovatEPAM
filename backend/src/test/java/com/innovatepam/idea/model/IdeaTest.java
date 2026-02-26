package com.innovatepam.idea.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;

class IdeaTest {
    
    private User submitter;
    private Idea idea;
        private Role submitterRole;
    
    @BeforeEach
    void setUp() {
            submitterRole = new Role();
            submitterRole.setId(UUID.randomUUID());
            submitterRole.setName("SUBMITTER");
            submitterRole.setCreatedAt(LocalDateTime.now());
        
        submitter = new User();
        submitter.setId(UUID.randomUUID());
        submitter.setEmail("submitter@example.com");
        submitter.setPasswordHash("hashed_password");
        submitter.setRole(submitterRole);
        submitter.setCreatedAt(LocalDateTime.now());
        
        idea = new Idea();
        idea.setTitle("Test Idea");
        idea.setDescription("This is a test idea description");
        idea.setCategory("Process Improvement");
        idea.setSubmitter(submitter);
    }
    
    @Test
    void testDefaultStatusIsSubmitted() {
        idea.onCreate();
        assertEquals(IdeaStatus.SUBMITTED, idea.getStatus(), "Default status should be SUBMITTED");
    }
    
    @Test
    void testOnCreateSetsTimestamps() {
        idea.onCreate();
        
        assertNotNull(idea.getCreatedAt(), "Created timestamp should be set");
        assertNotNull(idea.getUpdatedAt(), "Updated timestamp should be set");
        assertEquals(idea.getCreatedAt(), idea.getUpdatedAt(), "Initial timestamps should match");
    }
    
    @Test
    void testOnUpdateSetsUpdatedTimestamp() throws InterruptedException {
        idea.onCreate();
        LocalDateTime originalCreatedAt = idea.getCreatedAt();
        LocalDateTime originalUpdatedAt = idea.getUpdatedAt();
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);
        
        idea.onUpdate();
        
        assertEquals(originalCreatedAt, idea.getCreatedAt(), "Created timestamp should not change on update");
        assertTrue(idea.getUpdatedAt().isAfter(originalUpdatedAt), "Updated timestamp should be later");
    }
    
    @Test
    void testStatusTransitions() {
        idea.onCreate();
        assertEquals(IdeaStatus.SUBMITTED, idea.getStatus());
        
        idea.setStatus(IdeaStatus.UNDER_REVIEW);
        assertEquals(IdeaStatus.UNDER_REVIEW, idea.getStatus());
        
        idea.setStatus(IdeaStatus.ACCEPTED);
        assertEquals(IdeaStatus.ACCEPTED, idea.getStatus());
    }
    
    @Test
    void testAddEvaluation() {
            Role evaluatorRole = new Role();
            evaluatorRole.setId(UUID.randomUUID());
            evaluatorRole.setName("EVALUATOR");
            evaluatorRole.setCreatedAt(LocalDateTime.now());
        
        User evaluator = new User();
        evaluator.setId(UUID.randomUUID());
        evaluator.setEmail("evaluator@example.com");
        evaluator.setPasswordHash("hashed_password");
        evaluator.setRole(evaluatorRole);
        evaluator.setCreatedAt(LocalDateTime.now());
        
        IdeaEvaluation evaluation = new IdeaEvaluation();
        evaluation.setIdea(idea);
        evaluation.setEvaluator(evaluator);
        evaluation.setComment("Moving to review");
        evaluation.setStatusSnapshot(IdeaStatus.UNDER_REVIEW);
        evaluation.onCreate();
        
        idea.getEvaluations().add(evaluation);
        
        assertEquals(1, idea.getEvaluations().size(), "Should have one evaluation");
        assertEquals("Moving to review", idea.getEvaluations().get(0).getComment());
    }
    
    @Test
    void testAttachmentRelationship() {
        IdeaAttachment attachment = new IdeaAttachment();
        attachment.setIdea(idea);
        attachment.setOriginalFilename("proposal.pdf");
        attachment.setStoredFilename("uuid-proposal.pdf");
        attachment.setFileType(FileType.PDF);
        attachment.setFileSize(1024000L);
        attachment.setStorageLocation("/uploads/ideas/1/uuid-proposal.pdf");
        attachment.onCreate();
        
        idea.setAttachment(attachment);
        
        assertNotNull(idea.getAttachment(), "Idea should have an attachment");
        assertEquals("proposal.pdf", idea.getAttachment().getOriginalFilename());
        assertEquals(FileType.PDF, idea.getAttachment().getFileType());
    }
    
    @Test
    void testVersionField() {
        assertNull(idea.getVersion(), "Version should initially be null");
        
        idea.onCreate();
        // Version is typically managed by JPA, but we can set it manually for testing
        idea.setVersion(0);
        assertEquals(0, idea.getVersion());
    }
    
    @Test
    void testTitleValidation() {
        assertDoesNotThrow(() -> {
            idea.setTitle("Valid Title");
        });
    }
    
    @Test
    void testCategoryAssignment() {
        idea.setCategory("Innovation");
        assertEquals("Innovation", idea.getCategory());
        
        idea.setCategory("Cost Reduction");
        assertEquals("Cost Reduction", idea.getCategory());
    }
    
    @Test
    void testSubmitterRelationship() {
        assertEquals(submitter, idea.getSubmitter());
        assertEquals("submitter@example.com", idea.getSubmitter().getEmail());
        assertEquals("SUBMITTER", idea.getSubmitter().getRole().getName());
    }
}
