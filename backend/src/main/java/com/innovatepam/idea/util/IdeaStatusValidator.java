package com.innovatepam.idea.util;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.innovatepam.idea.model.IdeaStatus;

/**
 * Validates status transitions for ideas following the defined workflow.
 * 
 * Valid transitions:
 * - SUBMITTED → UNDER_REVIEW
 * - SUBMITTED → REJECTED (direct rejection allowed)
 * - UNDER_REVIEW → ACCEPTED
 * - UNDER_REVIEW → REJECTED
 */
public class IdeaStatusValidator {
    
    private static final Map<IdeaStatus, Set<IdeaStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(IdeaStatus.class);
    
    static {
        ALLOWED_TRANSITIONS.put(IdeaStatus.SUBMITTED, 
            EnumSet.of(IdeaStatus.UNDER_REVIEW, IdeaStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(IdeaStatus.UNDER_REVIEW, 
            EnumSet.of(IdeaStatus.ACCEPTED, IdeaStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(IdeaStatus.ACCEPTED, EnumSet.noneOf(IdeaStatus.class));
        ALLOWED_TRANSITIONS.put(IdeaStatus.REJECTED, EnumSet.noneOf(IdeaStatus.class));
    }
    
    /**
     * Checks if the transition from current status to new status is valid.
     * 
     * @param currentStatus The current status of the idea
     * @param newStatus The target status
     * @return true if transition is allowed, false otherwise
     */
    public static boolean isValidTransition(IdeaStatus currentStatus, IdeaStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        
        if (currentStatus == newStatus) {
            return false; // No self-transitions
        }
        
        Set<IdeaStatus> allowedTargets = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowedTargets != null && allowedTargets.contains(newStatus);
    }
    
    /**
     * Gets the set of allowed target statuses from a given current status.
     * 
     * @param currentStatus The current status
     * @return Set of allowed target statuses (empty if none allowed)
     */
    public static Set<IdeaStatus> getAllowedTransitions(IdeaStatus currentStatus) {
        if (currentStatus == null) {
            return EnumSet.noneOf(IdeaStatus.class);
        }
        return EnumSet.copyOf(ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(IdeaStatus.class)));
    }
    
    /**
     * Checks if a comment is required for the given status transition.
     * Comment is mandatory when transitioning to REJECTED status.
     * 
     * @param newStatus The target status
     * @return true if comment is required, false otherwise
     */
    public static boolean isCommentRequired(IdeaStatus newStatus) {
        return newStatus == IdeaStatus.REJECTED;
    }
}
