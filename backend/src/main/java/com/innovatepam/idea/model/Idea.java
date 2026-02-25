package com.innovatepam.idea.model;

import com.innovatepam.auth.model.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ideas")
public class Idea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IdeaStatus status = IdeaStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;

    @OneToOne(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
    private IdeaAttachment attachment;

    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<IdeaEvaluation> evaluations = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Integer version;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = IdeaStatus.SUBMITTED;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public IdeaStatus getStatus() {
        return status;
    }

    public void setStatus(IdeaStatus status) {
        this.status = status;
    }

    public User getSubmitter() {
        return submitter;
    }

    public void setSubmitter(User submitter) {
        this.submitter = submitter;
    }

    public IdeaAttachment getAttachment() {
        return attachment;
    }

    public void setAttachment(IdeaAttachment attachment) {
        this.attachment = attachment;
    }

    public List<IdeaEvaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<IdeaEvaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
