package com.innovatepam.idea.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovatepam.auth.AuthApplication;
import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.RoleRepository;
import com.innovatepam.auth.repository.UserRepository;
import com.innovatepam.auth.security.JwtService;
import com.innovatepam.idea.dto.UpdateIdeaStatusRequest;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaRepository;

@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class IdeaControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String submitterToken;
    private String evaluatorToken;
    private User submitter;
    private User evaluator;
    private Idea testIdea;
    private Role submitterRole;
    private Role evaluatorRole;

    @BeforeEach
    void setUp() {
        submitterRole = new Role();
        submitterRole.setId(UUID.randomUUID());
        submitterRole.setName("SUBMITTER");
        submitterRole.setCreatedAt(LocalDateTime.now());
        submitterRole = roleRepository.save(submitterRole);

        evaluatorRole = new Role();
        evaluatorRole.setId(UUID.randomUUID());
        evaluatorRole.setName("EVALUATOR");
        evaluatorRole.setCreatedAt(LocalDateTime.now());
        evaluatorRole = roleRepository.save(evaluatorRole);

        // Create submitter user
        submitter = new User();
        submitter.setId(UUID.randomUUID());
        submitter.setEmail("submitter@test.com");
        submitter.setPasswordHash(passwordEncoder.encode("password"));
        submitter.setRole(submitterRole);
        submitter.setCreatedAt(LocalDateTime.now());
        submitter = userRepository.save(submitter);
        submitterToken = jwtService.generateToken(submitter);

        // Create evaluator user
        evaluator = new User();
        evaluator.setId(UUID.randomUUID());
        evaluator.setEmail("evaluator@test.com");
        evaluator.setPasswordHash(passwordEncoder.encode("password"));
        evaluator.setRole(evaluatorRole);
        evaluator.setCreatedAt(LocalDateTime.now());
        evaluator = userRepository.save(evaluator);
        evaluatorToken = jwtService.generateToken(evaluator);

        // Create test idea
        testIdea = new Idea();
        testIdea.setTitle("Test Idea");
        testIdea.setDescription("Test Description");
        testIdea.setCategory("Process Improvement");
        testIdea.setSubmitter(submitter);
        testIdea.onCreate();
        testIdea = ideaRepository.save(testIdea);
    }

    @Test
    void testCreateIdeaWithoutFile() throws Exception {
        mockMvc.perform(multipart("/api/v1/ideas")
                .param("title", "New Idea")
                .param("description", "New Description")
                .param("category", "Innovation")
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Idea")))
                .andExpect(jsonPath("$.category", is("Innovation")))
                .andExpect(jsonPath("$.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.hasAttachment", is(false)))
                .andExpect(jsonPath("$.submitterName", is("submitter@test.com")));
    }

    @Test
    void testCreateIdeaWithPdfFile() throws Exception {
        MockMultipartFile filePart = new MockMultipartFile(
            "file", "proposal.pdf", "application/pdf", "PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ideas")
                .file(filePart)
                .param("title", "Idea with PDF")
                .param("description", "Description")
                .param("category", "Innovation")
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasAttachment", is(true)));
    }

    @Test
    void testCreateIdeaUnauthorized() throws Exception {
        mockMvc.perform(multipart("/api/v1/ideas")
                .param("title", "New Idea")
                .param("description", "Description")
                .param("category", "Innovation"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllIdeas() throws Exception {
        mockMvc.perform(get("/api/v1/ideas")
                .header("Authorization", "Bearer " + submitterToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", notNullValue()))
                .andExpect(jsonPath("$.pageable", notNullValue()));
    }

    @Test
    void testGetIdeaById() throws Exception {
        mockMvc.perform(get("/api/v1/ideas/" + testIdea.getId())
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testIdea.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Idea")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.submitterName", is("submitter@test.com")));
    }

    @Test
    void testGetIdeaByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/ideas/99999")
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateIdeaStatus() throws Exception {
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(IdeaStatus.UNDER_REVIEW, "Moving to review");

        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UNDER_REVIEW")));
    }

    @Test
    void testUpdateIdeaStatusUnauthorized() throws Exception {
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(IdeaStatus.UNDER_REVIEW, "Comment");

        // Submitter cannot update status
        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + submitterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateIdeaStatusInvalidTransition() throws Exception {
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(IdeaStatus.ACCEPTED, "Comment"); // Invalid: SUBMITTED -> ACCEPTED

        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/ideas")
                .header("Authorization", "Bearer " + submitterToken)
                .param("status", "SUBMITTED")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("SUBMITTED"))));
    }

    @Test
    void testCreateIdeaWithInvalidFileType() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "document.txt", "text/plain", "Text content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ideas")
                .file(invalidFile)
                .param("title", "New Idea")
                .param("description", "Description")
                .param("category", "Innovation")
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isBadRequest());
    }
}
