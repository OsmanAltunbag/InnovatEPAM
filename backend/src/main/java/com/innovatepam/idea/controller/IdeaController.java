package com.innovatepam.idea.controller;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.UserRepository;
import com.innovatepam.idea.dto.IdeaDetailResponse;
import com.innovatepam.idea.dto.IdeaResponse;
import com.innovatepam.idea.dto.PageResponse;
import com.innovatepam.idea.exception.UnauthorizedAccessException;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.service.FileStorageService;
import com.innovatepam.idea.service.IdeaService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/ideas")
@Validated
public class IdeaController {
    private final IdeaService ideaService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public IdeaController(
        IdeaService ideaService,
        FileStorageService fileStorageService,
        UserRepository userRepository
    ) {
        this.ideaService = ideaService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUBMITTER', 'ADMIN')")
    public ResponseEntity<IdeaResponse> createIdea(
        @RequestParam @NotBlank(message = "Title is required") @Size(max = 255, message = "Title cannot exceed 255 characters") String title,
        @RequestParam @NotBlank(message = "Description is required") String description,
        @RequestParam @NotBlank(message = "Category is required") @Size(max = 50, message = "Category cannot exceed 50 characters") String category,
        @RequestParam(required = false) MultipartFile file,
        Authentication authentication
    ) {
        User submitter = getCurrentUser(authentication);
        IdeaResponse idea = ideaService.createIdea(title, description, category, submitter, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(idea);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<IdeaResponse>> getAllIdeas(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) IdeaStatus status,
        @RequestParam(required = false) String category
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IdeaResponse> ideas;

        if (status != null && category != null) {
            ideas = ideaService.getIdeasByStatusAndCategory(status, category, pageable);
        } else if (status != null) {
            ideas = ideaService.getIdeasByStatus(status, pageable);
        } else if (category != null) {
            ideas = ideaService.getIdeasByCategory(category, pageable);
        } else {
            ideas = ideaService.getIdeas(pageable);
        }

        return ResponseEntity.ok(PageResponse.of(ideas));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IdeaDetailResponse> getIdeaById(@PathVariable Long id) {
        IdeaDetailResponse idea = ideaService.getIdeaDetailById(id);
        return ResponseEntity.ok(idea);
    }

    @GetMapping("/{ideaId}/attachments/{attachmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadAttachment(
        @PathVariable Long ideaId,
        @PathVariable Long attachmentId
    ) {
        Idea idea = ideaService.getIdeaById(ideaId);
        
        if (idea.getAttachment() == null || !idea.getAttachment().getId().equals(attachmentId)) {
            throw new UnauthorizedAccessException("Attachment not found for this idea");
        }

        Resource resource = fileStorageService.loadFileAsResource(idea.getAttachment());
        
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + idea.getAttachment().getOriginalFilename() + "\"")
            .body(resource);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        System.out.println("[DEBUG] Looking up user with email: " + email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                System.err.println("[ERROR] User not found in database: " + email);
                return new UnauthorizedAccessException("User not found: " + email);
            });
        System.out.println("[DEBUG] Found user: ID=" + user.getId() + ", Email=" + user.getEmail());
        return user;
    }
}
