package com.innovatepam.idea.util;

import com.innovatepam.idea.model.IdeaStatus;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdeaStatusValidatorTest {
    
    @Test
    void testSubmittedToUnderReview() {
        assertTrue(IdeaStatusValidator.isValidTransition(IdeaStatus.SUBMITTED, IdeaStatus.UNDER_REVIEW),
            "SUBMITTED → UNDER_REVIEW should be valid");
    }
    
    @Test
    void testSubmittedToRejected() {
        assertTrue(IdeaStatusValidator.isValidTransition(IdeaStatus.SUBMITTED, IdeaStatus.REJECTED),
            "SUBMITTED → REJECTED should be valid (direct rejection allowed)");
    }
    
    @Test
    void testSubmittedToAccepted() {
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.SUBMITTED, IdeaStatus.ACCEPTED),
            "SUBMITTED → ACCEPTED should be invalid (must go through review)");
    }
    
    @Test
    void testUnderReviewToAccepted() {
        assertTrue(IdeaStatusValidator.isValidTransition(IdeaStatus.UNDER_REVIEW, IdeaStatus.ACCEPTED),
            "UNDER_REVIEW → ACCEPTED should be valid");
    }
    
    @Test
    void testUnderReviewToRejected() {
        assertTrue(IdeaStatusValidator.isValidTransition(IdeaStatus.UNDER_REVIEW, IdeaStatus.REJECTED),
            "UNDER_REVIEW → REJECTED should be valid");
    }
    
    @Test
    void testUnderReviewToSubmitted() {
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.UNDER_REVIEW, IdeaStatus.SUBMITTED),
            "UNDER_REVIEW → SUBMITTED should be invalid (no backwards transition)");
    }
    
    @Test
    void testAcceptedToAnyStatus() {
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.ACCEPTED, IdeaStatus.SUBMITTED),
            "ACCEPTED → SUBMITTED should be invalid (terminal state)");
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.ACCEPTED, IdeaStatus.UNDER_REVIEW),
            "ACCEPTED → UNDER_REVIEW should be invalid (terminal state)");
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.ACCEPTED, IdeaStatus.REJECTED),
            "ACCEPTED → REJECTED should be invalid (terminal state)");
    }
    
    @Test
    void testRejectedToAnyStatus() {
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.REJECTED, IdeaStatus.SUBMITTED),
            "REJECTED → SUBMITTED should be invalid (terminal state)");
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.REJECTED, IdeaStatus.UNDER_REVIEW),
            "REJECTED → UNDER_REVIEW should be invalid (terminal state)");
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.REJECTED, IdeaStatus.ACCEPTED),
            "REJECTED → ACCEPTED should be invalid (terminal state)");
    }
    
    @Test
    void testSelfTransition() {
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.SUBMITTED, IdeaStatus.SUBMITTED),
            "SUBMITTED → SUBMITTED should be invalid (no self-transition)");
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.UNDER_REVIEW, IdeaStatus.UNDER_REVIEW),
            "UNDER_REVIEW → UNDER_REVIEW should be invalid (no self-transition)");
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.ACCEPTED, IdeaStatus.ACCEPTED),
            "ACCEPTED → ACCEPTED should be invalid (no self-transition)");
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.REJECTED, IdeaStatus.REJECTED),
            "REJECTED → REJECTED should be invalid (no self-transition)");
    }
    
    @Test
    void testNullCurrentStatus() {
        assertFalse(IdeaStatusValidator.isValidTransition(null, IdeaStatus.UNDER_REVIEW),
            "Null current status should be invalid");
    }
    
    @Test
    void testNullNewStatus() {
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.SUBMITTED, null),
            "Null new status should be invalid");
    }
    
    @Test
    void testBothNull() {
        assertFalse(IdeaStatusValidator.isValidTransition(null, null),
            "Both null statuses should be invalid");
    }
    
    @Test
    void testGetAllowedTransitionsFromSubmitted() {
        Set<IdeaStatus> allowed = IdeaStatusValidator.getAllowedTransitions(IdeaStatus.SUBMITTED);
        
        assertEquals(2, allowed.size(), "SUBMITTED should have 2 allowed transitions");
        assertTrue(allowed.contains(IdeaStatus.UNDER_REVIEW), "Should allow UNDER_REVIEW");
        assertTrue(allowed.contains(IdeaStatus.REJECTED), "Should allow REJECTED");
    }
    
    @Test
    void testGetAllowedTransitionsFromUnderReview() {
        Set<IdeaStatus> allowed = IdeaStatusValidator.getAllowedTransitions(IdeaStatus.UNDER_REVIEW);
        
        assertEquals(2, allowed.size(), "UNDER_REVIEW should have 2 allowed transitions");
        assertTrue(allowed.contains(IdeaStatus.ACCEPTED), "Should allow ACCEPTED");
        assertTrue(allowed.contains(IdeaStatus.REJECTED), "Should allow REJECTED");
    }
    
    @Test
    void testGetAllowedTransitionsFromAccepted() {
        Set<IdeaStatus> allowed = IdeaStatusValidator.getAllowedTransitions(IdeaStatus.ACCEPTED);
        
        assertTrue(allowed.isEmpty(), "ACCEPTED should have no allowed transitions (terminal state)");
    }
    
    @Test
    void testGetAllowedTransitionsFromRejected() {
        Set<IdeaStatus> allowed = IdeaStatusValidator.getAllowedTransitions(IdeaStatus.REJECTED);
        
        assertTrue(allowed.isEmpty(), "REJECTED should have no allowed transitions (terminal state)");
    }
    
    @Test
    void testGetAllowedTransitionsFromNull() {
        Set<IdeaStatus> allowed = IdeaStatusValidator.getAllowedTransitions(null);
        
        assertTrue(allowed.isEmpty(), "Null status should return empty set");
    }
    
    @Test
    void testIsCommentRequiredForRejected() {
        assertTrue(IdeaStatusValidator.isCommentRequired(IdeaStatus.REJECTED),
            "Comment should be required when transitioning to REJECTED");
    }
    
    @Test
    void testIsCommentNotRequiredForAccepted() {
        assertFalse(IdeaStatusValidator.isCommentRequired(IdeaStatus.ACCEPTED),
            "Comment should not be required for ACCEPTED");
    }
    
    @Test
    void testIsCommentNotRequiredForUnderReview() {
        assertFalse(IdeaStatusValidator.isCommentRequired(IdeaStatus.UNDER_REVIEW),
            "Comment should not be required for UNDER_REVIEW");
    }
    
    @Test
    void testIsCommentNotRequiredForSubmitted() {
        assertFalse(IdeaStatusValidator.isCommentRequired(IdeaStatus.SUBMITTED),
            "Comment should not be required for SUBMITTED");
    }
    
    @Test
    void testCompleteWorkflow() {
        // Test a complete workflow: SUBMITTED → UNDER_REVIEW → ACCEPTED
        assertTrue(IdeaStatusValidator.isValidTransition(IdeaStatus.SUBMITTED, IdeaStatus.UNDER_REVIEW),
            "Step 1: SUBMITTED → UNDER_REVIEW should be valid");
        assertTrue(IdeaStatusValidator.isValidTransition(IdeaStatus.UNDER_REVIEW, IdeaStatus.ACCEPTED),
            "Step 2: UNDER_REVIEW → ACCEPTED should be valid");
        assertFalse(IdeaStatusValidator.isValidTransition(IdeaStatus.ACCEPTED, IdeaStatus.UNDER_REVIEW),
            "Step 3: Cannot go back from ACCEPTED");
    }
    
    @Test
    void testDirectRejectionWorkflow() {
        // Test direct rejection: SUBMITTED → REJECTED
        assertTrue(IdeaStatusValidator.isValidTransition(IdeaStatus.SUBMITTED, IdeaStatus.REJECTED),
            "Direct rejection: SUBMITTED → REJECTED should be valid");
        assertTrue(IdeaStatusValidator.isCommentRequired(IdeaStatus.REJECTED),
            "Direct rejection should require comment");
    }
}
