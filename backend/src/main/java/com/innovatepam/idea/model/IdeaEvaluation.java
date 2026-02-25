package com.innovatepam.idea.model;

import java.time.LocalDateTime;

import com.innovatepam.auth.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "idea_evaluations")
public class IdeaEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idea_id", nullable = false)
    private Idea idea;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @NotBlank(message = "Comment is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_snapshot", length = 50)
    private IdeaStatus statusSnapshot;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Idea getIdea() {
        return idea;
    }

    public void setIdea(Idea idea) {
        this.idea = idea;
    }

    public User getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(User evaluator) {
        this.evaluator = evaluator;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public IdeaStatus getStatusSnapshot() {
        return statusSnapshot;
    }

    public void setStatusSnapshot(IdeaStatus statusSnapshot) {
        this.statusSnapshot = statusSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
