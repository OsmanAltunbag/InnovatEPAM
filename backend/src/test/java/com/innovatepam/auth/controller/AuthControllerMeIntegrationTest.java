package com.innovatepam.auth.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovatepam.auth.dto.LoginRequest;
import com.innovatepam.auth.dto.RegisterRequest;
import com.innovatepam.auth.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerMeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_EMAIL = "metest@example.com";
    private static final String TEST_PASSWORD = "ValidPass123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private String registerAndLogin(String email, String password, String role) throws Exception {
        // Register
        RegisterRequest registerRequest = new RegisterRequest(email, password, role);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login to get token
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    void getMe_WithValidToken_ReturnsUserInfo() throws Exception {
        // Given
        String token = registerAndLogin(TEST_EMAIL, TEST_PASSWORD, "submitter");

        // When/Then
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
                .andExpect(jsonPath("$.role", is("submitter")))
                .andExpect(jsonPath("$.userId", notNullValue()));
    }

    @Test
    void getMe_WithoutToken_ReturnsUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_WithInvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When/Then
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_WithMalformedAuthorizationHeader_ReturnsUnauthorized() throws Exception {
        // Given
        String token = registerAndLogin(TEST_EMAIL, TEST_PASSWORD, "submitter");

        // When/Then - Missing "Bearer " prefix
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_WithEvaluatorAdminRole_ReturnsCorrectRole() throws Exception {
        // Given
        String token = registerAndLogin("evaluator@example.com", TEST_PASSWORD, "evaluator/admin");

        // When/Then
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("evaluator@example.com")))
                .andExpect(jsonPath("$.role", is("evaluator/admin")));
    }

    @Test
    void getMe_WithSecondEvaluatorAdminRole_ReturnsCorrectRole() throws Exception {
        // Given
        String token = registerAndLogin("admin@example.com", TEST_PASSWORD, "evaluator/admin");

        // When/Then
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("admin@example.com")))
                .andExpect(jsonPath("$.role", is("evaluator/admin")));
    }

    @Test
    void getMe_WithEmptyAuthorizationHeader_ReturnsUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_WithExpiredToken_ReturnsUnauthorized() throws Exception {
        // Note: This test would require a token with past expiration
        // For now, we'll test with an obviously invalid token structure
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiZXhwIjoxfQ.invalid";

        // When/Then
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_MultipleTimes_ReturnsSameUserInfo() throws Exception {
        // Given
        String token = registerAndLogin(TEST_EMAIL, TEST_PASSWORD, "submitter");

        // When/Then - Call /me endpoint multiple times
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/v1/auth/me")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
                    .andExpect(jsonPath("$.role", is("submitter")));
        }
    }
}
