package com.innovatepam.idea.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;
import com.innovatepam.idea.dto.IdeaResponse;
import com.innovatepam.idea.exception.IdeaNotFoundException;
import com.innovatepam.idea.exception.InvalidStatusTransitionException;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaRepository;

@ExtendWith(MockitoExtension.class)
class IdeaServiceTest {

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private IdeaEvaluationService evaluationService;


    @InjectMocks
    private IdeaService ideaService;

    private User submitter;
    private User evaluator;
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
        idea.setId(1L);
        idea.setTitle("Test Idea");
        idea.setDescription("Description");
        idea.setCategory("Process Improvement");
        idea.setSubmitter(submitter);
        idea.setStatus(IdeaStatus.SUBMITTED);
        idea.onCreate();

    }

    @Test
    void testCreateIdeaWithoutFile() {
        when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> {
            Idea savedIdea = invocation.getArgument(0);
            savedIdea.setId(1L);
            return savedIdea;
        });

        IdeaResponse response = ideaService.createIdea("New Idea", "Description", "Innovation", submitter, null);

        assertNotNull(response);
        assertEquals("New Idea", response.title());
        assertEquals("Innovation", response.category());
        assertEquals(IdeaStatus.SUBMITTED, response.status());
        assertFalse(response.hasAttachment());

        verify(ideaRepository).save(any(Idea.class));
        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    void testCreateIdeaWithFile() throws Exception {
        when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> {
            Idea savedIdea = invocation.getArgument(0);
            savedIdea.setId(1L);
            return savedIdea;
        });

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        IdeaResponse response = ideaService.createIdea("New Idea", "Description", "Innovation", submitter, mockFile);

        assertNotNull(response);
        assertFalse(response.hasAttachment());

        verify(ideaRepository, times(2)).save(any(Idea.class));
        verify(fileStorageService).storeFile(eq(mockFile), any(Idea.class));
    }

    @Test
    void testGetAllIdeas() {
        Idea idea2 = new Idea();
        idea2.setId(2L);
        idea2.setTitle("Second Idea");
        idea2.setDescription("Description 2");
        idea2.setCategory("Cost Reduction");
        idea2.setSubmitter(submitter);
        idea2.setStatus(IdeaStatus.UNDER_REVIEW);
        idea2.onCreate();

        Page<Idea> page = new PageImpl<>(Arrays.asList(idea, idea2));
        when(ideaRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<IdeaResponse> result = ideaService.getIdeas(PageRequest.of(0, 10));

        assertEquals(2, result.getContent().size());
        assertEquals("Test Idea", result.getContent().get(0).title());
        assertEquals("Second Idea", result.getContent().get(1).title());
    }

    @Test
    void testGetIdeaById() {
        when(ideaRepository.findById(1L)).thenReturn(Optional.of(idea));

        Idea response = ideaService.getIdeaById(1L);

        assertNotNull(response);
        assertEquals("Test Idea", response.getTitle());
        assertEquals("Description", response.getDescription());
        assertEquals("submitter@example.com", response.getSubmitter().getEmail());
    }

    @Test
    void testGetIdeaByIdNotFound() {
        when(ideaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IdeaNotFoundException.class, () -> {
            ideaService.getIdeaById(99L);
        });
    }

    @Test
    void testUpdateStatusValidTransition() {
        when(ideaRepository.findById(1L)).thenReturn(Optional.of(idea));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Idea response = ideaService.updateStatus(1L, IdeaStatus.UNDER_REVIEW, evaluator, "Moving to review");

        assertEquals(IdeaStatus.UNDER_REVIEW, response.getStatus());

        ArgumentCaptor<Idea> captor = ArgumentCaptor.forClass(Idea.class);
        verify(ideaRepository).save(captor.capture());
        assertEquals(IdeaStatus.UNDER_REVIEW, captor.getValue().getStatus());

        verify(evaluationService).addStatusEvaluation(eq(idea), eq(evaluator), eq("Moving to review"), eq(IdeaStatus.UNDER_REVIEW));
    }

    @Test
    void testUpdateStatusInvalidTransition() {
        when(ideaRepository.findById(1L)).thenReturn(Optional.of(idea));

        assertThrows(InvalidStatusTransitionException.class, () -> {
            ideaService.updateStatus(1L, IdeaStatus.ACCEPTED, evaluator, "Comment");
        });

        verify(ideaRepository, never()).save(any());
        verify(evaluationService, never()).addStatusEvaluation(any(), any(), any(), any());
    }

    @Test
    void testUpdateStatusToRejectedWithoutComment() {
        when(ideaRepository.findById(1L)).thenReturn(Optional.of(idea));

        assertThrows(InvalidStatusTransitionException.class, () -> {
            ideaService.updateStatus(1L, IdeaStatus.REJECTED, evaluator, null);
        });

        verify(ideaRepository, never()).save(any());
    }

    @Test
    void testUpdateStatusUnauthorized() {
        when(ideaRepository.findById(1L)).thenReturn(Optional.of(idea));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Idea response = ideaService.updateStatus(1L, IdeaStatus.UNDER_REVIEW, submitter, "Comment");

        assertEquals(IdeaStatus.UNDER_REVIEW, response.getStatus());
        verify(ideaRepository).save(any(Idea.class));
    }

    @Test
    void testGetIdeasByStatus() {
        Page<Idea> page = new PageImpl<>(Arrays.asList(idea));
        when(ideaRepository.findByStatus(eq(IdeaStatus.SUBMITTED), any(PageRequest.class))).thenReturn(page);

        Page<IdeaResponse> result = ideaService.getIdeasByStatus(IdeaStatus.SUBMITTED, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals(IdeaStatus.SUBMITTED, result.getContent().get(0).status());
    }
}
