package com.innovatepam.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovatepam.auth.dto.LoginRequest;
import com.innovatepam.auth.dto.RegisterRequest;
import com.innovatepam.auth.repository.AuthenticationAttemptRepository;
import com.innovatepam.auth.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationAttemptRepository authenticationAttemptRepository;

    private static final String TEST_EMAIL = "logintest@example.com";
    private static final String TEST_PASSWORD = "ValidPass123";

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        authenticationAttemptRepository.deleteAll();
        
        // Register a test user
        RegisterRequest registerRequest = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, "submitter");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));
    }

    @Test
    void login_WithValidCredentials_ReturnsOkAndToken() throws Exception {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
                .andExpect(jsonPath("$.role", is("submitter")))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.expiresIn", greaterThan(0)));
    }

    @Test
    void login_WithInvalidPassword_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, "WrongPassword123");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid")));
    }

    @Test
    void login_WithNonExistentEmail_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("nonexistent@example.com", "SomePassword123");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid")));
    }

    @Test
    void login_WithInvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("not-an-email", "SomePassword123");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithMissingEmail_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"password\":\"SomePassword123\"}";

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithMissingPassword_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"email\":\"user@example.com\"}";

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_After5FailedAttempts_ReturnsLocked() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest(TEST_EMAIL, "WrongPassword");

        // When - Make 5 failed login attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));
        }

        // Then - 6th attempt should be locked
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("locked")));
    }

    @Test
    void login_WithLockedAccount_EvenWithValidPassword_ReturnsLocked() throws Exception {
        // Given - Lock account with 5 failed attempts
        LoginRequest invalidRequest = new LoginRequest(TEST_EMAIL, "WrongPassword");
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));
        }

        // When - Try with VALID password after lockout
        LoginRequest validRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("locked")));
    }

    @Test
    void login_WithEmptyBody_ReturnsBadRequest() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
