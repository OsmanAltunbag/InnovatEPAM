# Quick Start: Idea Management System Integration

**Feature**: Idea Management System  
**Branch**: `002-idea-management`  
**Date**: February 25, 2026  

---

## Overview

This guide helps developers integrate the Idea Management System into the InnovatEPAM portal. The feature extends the existing JWT authentication system with idea submission, listing, and evaluation workflows.

## Prerequisites

- Java 21+ installed
- Spring Boot 3.2.2+ running
- PostgreSQL 13+ running
- Node.js 18+ installed
- React 18+ installed
- JWT token from authentication system (for testing)

## Backend Setup

### 1. Apply Database Migrations

Migrations are automatically applied when the Spring Boot application starts via Flyway.

**Manual migration** (if needed):

```bash
cd backend
mvn flyway:migrate
```

This creates three new tables:
- `ideas` - Core idea storage
- `idea_evaluations` - Evaluator feedback
- `idea_attachments` - File metadata

**Verify migrations**:

```bash
psql -U postgres -d innovatepam -c "
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;
"
```

You should see: `ideas`, `idea_attachments`, `idea_evaluations` tables.

### 2. Create Backend Package Structure

Create the feature package under `backend/src/main/java/com/innovatepam/`:

```bash
mkdir -p backend/src/main/java/com/innovatepam/idea/{model,repository,service,controller,dto,exception,util}
mkdir -p backend/src/test/java/com/innovatepam/idea/{model,service,controller,repository,util}
```

### 3. Implement Core Entities

Create JPA entity classes in `model/`:

**File**: `backend/src/main/java/com/innovatepam/idea/model/Idea.java`

(See `data-model.md` for full entity implementation)

**File**: `backend/src/main/java/com/innovatepam/idea/model/IdeaEvaluation.java`

(See `data-model.md` for full entity implementation)

**File**: `backend/src/main/java/com/innovatepam/idea/model/IdeaAttachment.java`

(See `data-model.md` for full entity implementation)

### 4. Implement Repositories

Create repository interfaces in `repository/`:

**File**: `backend/src/main/java/com/innovatepam/idea/repository/IdeaRepository.java`

```java
package com.innovatepam.idea.repository;

import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdeaRepository extends JpaRepository<Idea, Long> {
    Page<Idea> findAll(Pageable pageable);
    Page<Idea> findByStatus(IdeaStatus status, Pageable pageable);
    Page<Idea> findBySubmitterId(UUID submitterId, Pageable pageable);
}
```

Similarly, implement `IdeaEvaluationRepository` and `IdeaAttachmentRepository` (see `data-model.md`).

### 5. Implement Services

Create service classes in `service/` to handle business logic:

**File**: `backend/src/main/java/com/innovatepam/idea/service/IdeaService.java`

```java
package com.innovatepam.idea.service;

import com.innovatepam.auth.model.User;
import com.innovatepam.idea.dto.CreateIdeaRequest;
import com.innovatepam.idea.dto.IdeaResponse;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdeaService {

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public IdeaResponse createIdea(CreateIdeaRequest request, User submitter) {
        // Validate input
        validateIdeaRequest(request);
        
        // Create idea entity
        Idea idea = new Idea();
        idea.setTitle(request.getTitle());
        idea.setDescription(request.getDescription());
        idea.setCategory(request.getCategory());
        idea.setSubmitter(submitter);
        idea.setStatus(IdeaStatus.SUBMITTED);
        
        // Handle file upload if present
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            // Validate and store file
            IdeaAttachment attachment = fileStorageService.storeFile(request.getFile(), idea);
            idea.setAttachment(attachment);
        }
        
        // Persist idea
        Idea savedIdea = ideaRepository.save(idea);
        return mapToResponse(savedIdea);
    }

    @Transactional(readOnly = true)
    public Page<IdeaResponse> getAllIdeas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ideaRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public IdeaResponse getIdeaById(Long id) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new IdeaNotFoundException("Idea not found: " + id));
        return mapToDetailResponse(idea);
    }

    @Transactional
    public IdeaResponse updateIdeaStatus(Long id, String newStatus, String comment, User evaluator) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new IdeaNotFoundException("Idea not found: " + id));
        
        // Validate status transition
        IdeaStatus targetStatus = IdeaStatus.valueOf(newStatus);
        validateStatusTransition(idea.getStatus(), targetStatus, comment);
        
        // Update status and add evaluation
        idea.setStatus(targetStatus);
        IdeaEvaluation evaluation = new IdeaEvaluation();
        evaluation.setIdea(idea);
        evaluation.setEvaluator(evaluator);
        evaluation.setComment(comment);
        evaluation.setStatusSnapshot(targetStatus);
        idea.getEvaluations().add(evaluation);
        
        Idea savedIdea = ideaRepository.save(idea);
        return mapToResponse(savedIdea);
    }

    private void validateIdeaRequest(CreateIdeaRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }
    }

    private void validateStatusTransition(IdeaStatus current, IdeaStatus target, String comment) {
        boolean isValidTransition = false;
        
        if (current == IdeaStatus.SUBMITTED) {
            isValidTransition = target == IdeaStatus.UNDER_REVIEW || target == IdeaStatus.REJECTED;
        } else if (current == IdeaStatus.UNDER_REVIEW) {
            isValidTransition = target == IdeaStatus.ACCEPTED || target == IdeaStatus.REJECTED;
        }
        
        if (!isValidTransition) {
            throw new InvalidStatusTransitionException(
                "Cannot transition from " + current + " to " + target);
        }
        
        // Comment required for rejection
        if (target == IdeaStatus.REJECTED && (comment == null || comment.isBlank())) {
            throw new IllegalArgumentException("Comment required when rejecting an idea");
        }
    }

    private IdeaResponse mapToResponse(Idea idea) {
        return new IdeaResponse(
            idea.getId(),
            idea.getTitle(),
            idea.getCategory(),
            idea.getStatus().toString(),
            idea.getSubmitter().getEmail(), // or getName if available
            idea.getCreatedAt(),
            idea.getAttachment() != null,
            idea.getEvaluations().size()
        );
    }

    private IdeaResponse mapToDetailResponse(Idea idea) {
        // More complex mapping with details; implement based on IdeaDetailDto
        return mapToResponse(idea); // simplified for this example
    }
}
```

**File**: `backend/src/main/java/com/innovatepam/idea/service/FileStorageService.java`

```java
package com.innovatepam.idea.service;

import com.innovatepam.idea.exception.InvalidFileException;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaAttachment;
import com.innovatepam.idea.model.FileType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${idea.upload.directory:/uploads/ideas}")
    private String uploadDirectory;

    @Value("${idea.upload.max-file-size:52428800}")  // 50MB
    private long maxFileSize;

    private static final String[] ALLOWED_MIME_TYPES = {"application/pdf", "image/png"};
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".png"};

    public IdeaAttachment storeFile(MultipartFile file, Idea idea) {
        // Validate file
        validateFile(file);
        
        try {
            // Create storage directory if not exists
            Path uploadPath = Paths.get(uploadDirectory, idea.getId().toString());
            Files.createDirectories(uploadPath);
            
            // Generate unique filename
            String originalName = file.getOriginalFilename();
            String storedName = UUID.randomUUID().toString() + getFileExtension(originalName);
            Path filePath = uploadPath.resolve(storedName);
            
            // Save file
            Files.write(filePath, file.getBytes());
            
            // Create attachment record
            IdeaAttachment attachment = new IdeaAttachment();
            attachment.setIdea(idea);
            attachment.setOriginalFilename(originalName);
            attachment.setStoredFilename(storedName);
            attachment.setFileType(getFileType(originalName));
            attachment.setFileSize(file.getSize());
            attachment.setStorageLocation(filePath.toString());
            
            return attachment;
        } catch (IOException e) {
            throw new InvalidFileException("Failed to store file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }
        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException("File size exceeds maximum allowed size");
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        // Validate MIME type
        boolean validMimeType = false;
        for (String allowedType : ALLOWED_MIME_TYPES) {
            if (contentType != null && contentType.equals(allowedType)) {
                validMimeType = true;
                break;
            }
        }
        if (!validMimeType) {
            throw new InvalidFileException("File type not allowed. Only PDF and PNG files permitted.");
        }
        
        // Validate extension
        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (filename != null && filename.toLowerCase().endsWith(ext)) {
                validExtension = true;
                break;
            }
        }
        if (!validExtension) {
            throw new InvalidFileException("File extension not allowed.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private FileType getFileType(String filename) {
        if (filename != null) {
            if (filename.toLowerCase().endsWith(".pdf")) {
                return FileType.PDF;
            } else if (filename.toLowerCase().endsWith(".png")) {
                return FileType.PNG;
            }
        }
        throw new InvalidFileException("Unknown file type");
    }
}
```

### 6. Implement Controllers

Create controller classes in `controller/`:

**File**: `backend/src/main/java/com/innovatepam/idea/controller/IdeaController.java`

```java
package com.innovatepam.idea.controller;

import com.innovatepam.auth.model.User;
import com.innovatepam.idea.dto.CreateIdeaRequest;
import com.innovatepam.idea.dto.IdeaResponse;
import com.innovatepam.idea.service.IdeaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ideas")
public class IdeaController {

    @Autowired
    private IdeaService ideaService;

    @PostMapping
    @PreAuthorize("hasRole('SUBMITTER') or hasRole('ADMIN')")
    public ResponseEntity<IdeaResponse> createIdea(
            @ModelAttribute CreateIdeaRequest request,
            Authentication authentication) {
        
        // Extract user from authentication
        User submitter = (User) authentication.getPrincipal();
        
        // Create idea
        IdeaResponse response = ideaService.createIdea(request, submitter);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<IdeaResponse>> getAllIdeas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<IdeaResponse> ideas = ideaService.getAllIdeas(page, size);
        return ResponseEntity.ok(ideas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IdeaResponse> getIdeaById(@PathVariable Long id) {
        IdeaResponse idea = ideaService.getIdeaById(id);
        return ResponseEntity.ok(idea);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EVALUATOR') or hasRole('ADMIN')")
    public ResponseEntity<IdeaResponse> updateIdeaStatus(
            @PathVariable Long id,
            @RequestBody UpdateIdeaStatusRequest request,
            Authentication authentication) {
        
        User evaluator = (User) authentication.getPrincipal();
        IdeaResponse response = ideaService.updateIdeaStatus(id, request.getNewStatus(), request.getComment(), evaluator);
        
        return ResponseEntity.ok(response);
    }
}
```

### 7. Configure Application Properties

**File**: `backend/src/main/resources/application.yml`

Add configuration for file uploads:

```yaml
# Existing configuration...

# Idea Management Configuration
idea:
  upload:
    directory: /uploads/ideas
    max-file-size: 52428800  # 50MB in bytes
    allowed-types: application/pdf,image/png

# Multipart file upload limits
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

### 8. Write Tests

Create unit and integration tests in `src/test/`:

**File**: `backend/src/test/java/com/innovatepam/idea/service/IdeaServiceTest.java`

```java
package com.innovatepam.idea.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.innovatepam.auth.model.User;
import com.innovatepam.idea.dto.CreateIdeaRequest;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class IdeaServiceTest {

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private IdeaService ideaService;

    private User testUser;
    private CreateIdeaRequest testRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("submitter@example.com");
        
        testRequest = new CreateIdeaRequest();
        testRequest.setTitle("Test Idea");
        testRequest.setDescription("Test Description");
        testRequest.setCategory("Test Category");
    }

    @Test
    void testCreateIdea_Success() {
        // Arrange
        Idea savedIdea = new Idea();
        savedIdea.setId(1L);
        savedIdea.setTitle("Test Idea");
        when(ideaRepository.save(any(Idea.class))).thenReturn(savedIdea);
        
        // Act
        ideaService.createIdea(testRequest, testUser);
        
        // Assert
        verify(ideaRepository, times(1)).save(any(Idea.class));
    }

    @Test
    void testCreateIdea_InvalidTitle() {
        // Arrange
        testRequest.setTitle("");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> ideaService.createIdea(testRequest, testUser));
    }
}
```

### 9. Run Backend Tests

```bash
cd backend
mvn clean test

# Check coverage
mvn jacoco:report
# Coverage report: target/site/jacoco/index.html
```

**Target**: 80% line coverage (enforced by JaCoCo)

---

## Frontend Setup

### 1. Create Component Structure

Create feature components under `frontend/src/`:

```bash
mkdir -p frontend/src/components/ideas
mkdir -p frontend/src/pages/ideas
mkdir -p frontend/src/services/ideas
mkdir -p frontend/src/hooks/ideas
mkdir -p frontend/src/test/ideas
```

### 2. Create API Service

**File**: `frontend/src/services/ideaService.js`

```javascript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
const API = axios.create({
    baseURL: API_BASE_URL
});

// Add JWT token to requests
API.interceptors.request.use((config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Create idea with file upload
export const createIdea = async (title, description, category, file) => {
    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('category', category);
    if (file) {
        formData.append('file', file);
    }
    
    return API.post('/ideas', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
};

// Get all ideas with pagination
export const getAllIdeas = async (page = 0, size = 10) => {
    return API.get('/ideas', { params: { page, size } });
};

// Get single idea detail
export const getIdeaById = async (id) => {
    return API.get(`/ideas/${id}`);
};

// Update idea status
export const updateIdeaStatus = async (id, newStatus, comment) => {
    return API.patch(`/ideas/${id}/status`, {
        newStatus,
        comment
    });
};

// Add evaluation comment
export const addEvaluationComment = async (id, comment) => {
    return API.post(`/ideas/${id}/comments`, { comment });
};

// Download attachment
export const downloadAttachment = async (ideaId, attachmentId) => {
    return API.get(`/ideas/${ideaId}/attachments/${attachmentId}`, {
        responseType: 'blob'
    });
};

export default API;
```

### 3. Create React Components

**File**: `frontend/src/components/ideas/IdeaForm.jsx`

```jsx
import React, { useState } from 'react';
import { createIdea } from '../../services/ideaService';
import FileUpload from './FileUpload';

export default function IdeaForm({ onSuccess }) {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        category: '',
        file: null
    });
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleFileSelect = (file) => {
        setFormData(prev => ({
            ...prev,
            file
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const response = await createIdea(
                formData.title,
                formData.description,
                formData.category,
                formData.file
            );
            
            if (onSuccess) {
                onSuccess(response.data);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to submit idea');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 max-w-2xl">
            <h2 className="text-2xl font-bold mb-6">Submit Your Idea</h2>
            
            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
                    {error}
                </div>
            )}

            <div className="mb-4">
                <label className="block text-gray-700 font-semibold mb-2">
                    Title <span className="text-red-500">*</span>
                </label>
                <input
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleInputChange}
                    maxLength="255"
                    required
                    className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="What's your idea?"
                />
            </div>

            <div className="mb-4">
                <label className="block text-gray-700 font-semibold mb-2">
                    Description <span className="text-red-500">*</span>
                </label>
                <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleInputChange}
                    required
                    rows="5"
                    className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Describe your idea in detail..."
                />
            </div>

            <div className="mb-4">
                <label className="block text-gray-700 font-semibold mb-2">
                    Category <span className="text-red-500">*</span>
                </label>
                <select
                    name="category"
                    value={formData.category}
                    onChange={handleInputChange}
                    required
                    className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                    <option value="">Select a category</option>
                    <option value="Process Improvement">Process Improvement</option>
                    <option value="Cost Reduction">Cost Reduction</option>
                    <option value="Sustainability">Sustainability</option>
                    <option value="Innovation">Innovation</option>
                </select>
            </div>

            <div className="mb-6">
                <label className="block text-gray-700 font-semibold mb-2">
                    Attachment (Optional)
                </label>
                <FileUpload onFileSelect={handleFileSelect} />
            </div>

            <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-2 px-4 rounded"
            >
                {loading ? 'Submitting...' : 'Submit Idea'}
            </button>
        </form>
    );
}
```

### 4. Create API Mocks (MSW)

**File**: `frontend/src/test/mocks/ideaHandlers.js`

```javascript
import { http, HttpResponse } from 'msw';

const API_BASE_URL = 'http://localhost:8080/api/v1';

export const ideaHandlers = [
    // Create idea
    http.post(`${API_BASE_URL}/ideas`, async ({ request }) => {
        return HttpResponse.json({
            id: 1,
            title: 'Test Idea',
            category: 'Process Improvement',
            status: 'SUBMITTED',
            submitterName: 'User',
            createdAt: new Date().toISOString(),
            hasAttachment: false,
            evaluationCount: 0
        }, { status: 201 });
    }),

    // Get all ideas
    http.get(`${API_BASE_URL}/ideas`, () => {
        return HttpResponse.json({
            content: [
                {
                    id: 1,
                    title: 'Test Idea',
                    category: 'Innovation',
                    status: 'SUBMITTED',
                    submitterName: 'Alice Johnson',
                    createdAt: '2026-02-25T14:30:00Z',
                    hasAttachment: true,
                    evaluationCount: 0
                }
            ],
            pageable: {
                pageNumber: 0,
                pageSize: 10,
                totalElements: 1,
                totalPages: 1
            }
        });
    }),

    // Get idea details
    http.get(`${API_BASE_URL}/ideas/:id`, ({ params }) => {
        return HttpResponse.json({
            id: params.id,
            title: 'Test Idea',
            description: 'Full description here',
            category: 'Innovation',
            status: 'SUBMITTED',
            submitterName: 'Alice Johnson',
            createdAt: '2026-02-25T14:30:00Z',
            updatedAt: '2026-02-25T14:30:00Z',
            attachment: null,
            evaluations: []
        });
    })
];
```

### 5. Run Frontend Tests

```bash
cd frontend
npm test -- --run

# Check coverage
npm test -- --coverage
```

**Target**: 80% line coverage

---

## Integration Testing

### Backend Integration Test Example

**File**: `backend/src/test/java/com/innovatepam/idea/controller/IdeaControllerIntegrationTest.java`

```java
package com.innovatepam.idea.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class IdeaControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("innovatepam_test")
        .withUsername("postgres")
        .withPassword("password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = {"SUBMITTER"})
    void testCreateIdea_Success() throws Exception {
        // Test implementation
        mockMvc.perform(post("/api/v1/ideas")
            .contentType("application/json")
            .content("{\"title\":\"Test\",\"description\":\"Test\",\"category\":\"Test\"}"))
            .andExpect(status().isCreated());
    }
}
```

---

## Common Tasks

### Run Backend Locally

```bash
cd backend
mvn clean spring-boot:run
```

Backend runs on `http://localhost:8080`

### Run Frontend Locally

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`

### Test the Full Flow

1. **Register a submitter**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "email": "submitter@example.com",
       "password": "Password123!",
       "role": "submitter"
     }'
   ```

2. **Get JWT token**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "email": "submitter@example.com",
       "password": "Password123!"
     }'
   ```

3. **Create an idea**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/ideas \
     -H "Authorization: Bearer {JWT_TOKEN}" \
     -F "title=Test Idea" \
     -F "description=A great idea" \
     -F "category=Innovation" \
     -F "file=@proposal.pdf"
   ```

---

## Troubleshooting

**PostgreSQL connection fails**:
- Ensure PostgreSQL is running: `psql --version`
- Check database exists: `psql -l` should show `innovatepam`

**File uploads fail with 413**:
- Check `spring.servlet.multipart.max-file-size` in `application.yml`
- Ensure file is < 50MB

**JWT token invalid**:
- Verify token is included in `Authorization: Bearer {token}`
- Check token hasn't expired

**Tests fail with "No such table"**:
- Ensure Flyway migrations are applied: `mvn flyway:info`
- Run migrations: `mvn flyway:migrate`

---

## Next Steps

1. **Implement remaining services**: FileStorageService, IdeaEvaluationService (see plan.md)
2. **Create React components**: IdeaListing, IdeaDetail, EvaluationPanel (see plan.md)
3. **Write comprehensive tests**: Aim for 80%+ coverage
4. **Set up CI/CD**: Configure GitHub Actions to run tests on pull requests
5. **Deploy to staging**: Test end-to-end in staging environment before production release
