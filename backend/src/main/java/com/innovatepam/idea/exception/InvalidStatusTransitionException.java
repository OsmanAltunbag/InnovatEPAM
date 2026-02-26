package com.innovatepam.idea.exception;

import com.innovatepam.idea.model.IdeaStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    private final IdeaStatus currentStatus;
    private final IdeaStatus targetStatus;

    public InvalidStatusTransitionException(IdeaStatus currentStatus, IdeaStatus targetStatus) {
        super("Cannot transition from " + currentStatus + " to " + targetStatus);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public IdeaStatus getCurrentStatus() {
        return currentStatus;
    }

    public IdeaStatus getTargetStatus() {
        return targetStatus;
    }
}
