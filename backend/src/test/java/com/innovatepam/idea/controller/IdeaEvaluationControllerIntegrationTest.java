package com.innovatepam.idea.controller;

import java.time.LocalDateTime;
import java.util.UUID;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.innovatepam.idea.dto.AddCommentRequest;
import com.innovatepam.idea.dto.UpdateIdeaStatusRequest;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;
import com.innovatepam.idea.repository.IdeaRepository;

@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class IdeaEvaluationControllerIntegrationTest {

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
    private String adminToken;
    private User submitter;
    private User evaluator;
    private User admin;
    private Idea testIdea;
    private Role submitterRole;
    private Role evaluatorRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Create roles
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

        adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("ADMIN");
        adminRole.setCreatedAt(LocalDateTime.now());
        adminRole = roleRepository.save(adminRole);

        // Create users
        submitter = new User();
        submitter.setId(UUID.randomUUID());
        submitter.setEmail("submitter@test.com");
        submitter.setPasswordHash(passwordEncoder.encode("password"));
        submitter.setRole(submitterRole);
        submitter.setCreatedAt(LocalDateTime.now());
        submitter = userRepository.save(submitter);
        submitterToken = jwtService.generateToken(submitter);

        evaluator = new User();
        evaluator.setId(UUID.randomUUID());
        evaluator.setEmail("evaluator@test.com");
        evaluator.setPasswordHash(passwordEncoder.encode("password"));
        evaluator.setRole(evaluatorRole);
        evaluator.setCreatedAt(LocalDateTime.now());
        evaluator = userRepository.save(evaluator);
        evaluatorToken = jwtService.generateToken(evaluator);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("password"));
        admin.setRole(adminRole);
        admin.setCreatedAt(LocalDateTime.now());
        admin = userRepository.save(admin);
        adminToken = jwtService.generateToken(admin);

        // Create test idea
        testIdea = new Idea();
        testIdea.setTitle("Test Idea");
        testIdea.setDescription("Test Description");
        testIdea.setCategory("Process Improvement");
        testIdea.setSubmitter(submitter);
        testIdea.setStatus(IdeaStatus.SUBMITTED);
        testIdea.onCreate();
        testIdea = ideaRepository.save(testIdea);
    }

    @Test
    void testUpdateIdeaStatusByEvaluator() throws Exception {
        // Arrange
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(
            IdeaStatus.UNDER_REVIEW,
            "Moving to review"
        );

        // Act & Assert
        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UNDER_REVIEW")))
                .andExpect(jsonPath("$.id", is(testIdea.getId().intValue())));
    }

    @Test
    void testUpdateIdeaStatusByAdmin() throws Exception {
        // Arrange
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(
            IdeaStatus.UNDER_REVIEW,
            "Admin review"
        );

        // Act & Assert
        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UNDER_REVIEW")));
    }

    @Test
    void testUpdateIdeaStatusForbiddenForSubmitter() throws Exception {
        // Arrange
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(
            IdeaStatus.UNDER_REVIEW,
            "Comment"
        );

        // Act & Assert
        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + submitterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateIdeaStatusUnauthorized() throws Exception {
        // Arrange
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(
            IdeaStatus.UNDER_REVIEW,
            "Comment"
        );

        // Act & Assert
        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateIdeaStatusWithInvalidTransition() throws Exception {
        // Arrange - SUBMITTED -> ACCEPTED is invalid (must go through UNDER_REVIEW)
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(
            IdeaStatus.ACCEPTED,
            "Direct acceptance"
        );

        // Act & Assert
        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateIdeaStatusWithCommentRequired() throws Exception {
        // Arrange - Move idea to UNDER_REVIEW first
        testIdea.setStatus(IdeaStatus.UNDER_REVIEW);
        ideaRepository.save(testIdea);

        // Try to reject without comment
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(
            IdeaStatus.REJECTED,
            null // No comment provided
        );

        // Act & Assert
        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateIdeaStatusWithValidComment() throws Exception {
        // Arrange - Move idea to UNDER_REVIEW first
        testIdea.setStatus(IdeaStatus.UNDER_REVIEW);
        ideaRepository.save(testIdea);

        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest(
            IdeaStatus.REJECTED,
            "Not feasible at the moment"
        );

        // Act & Assert
        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    void testAddCommentByEvaluator() throws Exception {
        // Arrange
        AddCommentRequest request = new AddCommentRequest("This is a great idea!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/ideas/" + testIdea.getId() + "/comments")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment", is("This is a great idea!")))
                .andExpect(jsonPath("$.evaluator", notNullValue()));
    }

    @Test
    void testAddCommentByAdmin() throws Exception {
        // Arrange
        AddCommentRequest request = new AddCommentRequest("Admin feedback on the idea");

        // Act & Assert
        mockMvc.perform(post("/api/v1/ideas/" + testIdea.getId() + "/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment", is("Admin feedback on the idea")));
    }

    @Test
    void testAddCommentForbiddenForSubmitter() throws Exception {
        // Arrange
        AddCommentRequest request = new AddCommentRequest("Submitter comment");

        // Act & Assert
        mockMvc.perform(post("/api/v1/ideas/" + testIdea.getId() + "/comments")
                .header("Authorization", "Bearer " + submitterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAddCommentUnauthorized() throws Exception {
        // Arrange
        AddCommentRequest request = new AddCommentRequest("Unauthorized comment");

        // Act & Assert
        mockMvc.perform(post("/api/v1/ideas/" + testIdea.getId() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddCommentToNonExistentIdea() throws Exception {
        // Arrange
        AddCommentRequest request = new AddCommentRequest("Comment on non-existent idea");

        // Act & Assert
        mockMvc.perform(post("/api/v1/ideas/99999/comments")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetEvaluationHistory() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/ideas/" + testIdea.getId() + "/evaluations")
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ideaId", is(testIdea.getId().intValue())))
                .andExpect(jsonPath("$.evaluations", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void testGetEvaluationHistoryUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/ideas/" + testIdea.getId() + "/evaluations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetEvaluationHistoryForNonExistentIdea() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/ideas/99999/evaluations")
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testMultipleEvaluations() throws Exception {
        // Arrange - Add multiple comments
        AddCommentRequest comment1 = new AddCommentRequest("First evaluation");
        AddCommentRequest comment2 = new AddCommentRequest("Second evaluation");

        mockMvc.perform(post("/api/v1/ideas/" + testIdea.getId() + "/comments")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/ideas/" + testIdea.getId() + "/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment2)))
                .andExpect(status().isCreated());

        // Act & Assert - Get evaluation history
        mockMvc.perform(get("/api/v1/ideas/" + testIdea.getId() + "/evaluations")
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluations", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void testCompleteEvaluationWorkflow() throws Exception {
        // Step 1: Move idea to UNDER_REVIEW
        UpdateIdeaStatusRequest statusRequest = new UpdateIdeaStatusRequest(
            IdeaStatus.UNDER_REVIEW,
            "Moving to review"
        );

        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk());

        // Step 2: Add comment
        AddCommentRequest commentRequest = new AddCommentRequest("Needs more details");

        mockMvc.perform(post("/api/v1/ideas/" + testIdea.getId() + "/comments")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated());

        // Step 3: Accept the idea
        UpdateIdeaStatusRequest acceptRequest = new UpdateIdeaStatusRequest(
            IdeaStatus.ACCEPTED,
            "After revision, this idea looks good"
        );

        mockMvc.perform(patch("/api/v1/ideas/" + testIdea.getId() + "/status")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")));

        // Step 4: Verify evaluation history
        mockMvc.perform(get("/api/v1/ideas/" + testIdea.getId() + "/evaluations")
                .header("Authorization", "Bearer " + submitterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluations", hasSize(greaterThanOrEqualTo(2))));
    }
}
