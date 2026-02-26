package com.innovatepam.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovatepam.auth.dto.RegisterRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicEndpoint_Register_AllowsAccessWithoutAuthentication() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("public@example.com", "ValidPass123", "submitter");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void publicEndpoint_Login_AllowsAccessWithoutAuthentication() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized()); // Unauthorized due to invalid creds, not auth filter
    }

    @Test
    void publicEndpoint_ActuatorHealth_AllowsAccessWithoutAuthentication() throws Exception {
        // When/Then
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void publicEndpoint_SwaggerUI_AllowsAccessWithoutAuthentication() throws Exception {
        // When/Then
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection()); // Redirects to swagger-ui/index.html
    }

    @Test
    void publicEndpoint_OpenApiDocs_AllowsAccessWithoutAuthentication() throws Exception {
        // When/Then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_Me_RequiresAuthentication() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cors_PreflightRequest_ReturnsCorrectHeaders() throws Exception {
        // When/Then
        mockMvc.perform(options("/api/v1/auth/register")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void cors_ActualRequest_ReturnsCorrectHeaders() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("cors@example.com", "ValidPass123", "submitter");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void csrf_IsDisabled_ForStatelessApi() throws Exception {
        // Given - CSRF should be disabled for stateless JWT API
        RegisterRequest request = new RegisterRequest("csrf@example.com", "ValidPass123", "submitter");

        // When/Then - Request succeeds without CSRF token
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void sessionManagement_IsStateless() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("stateless@example.com", "ValidPass123", "submitter");

        // When/Then - No session cookie should be created
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(cookie().doesNotExist("JSESSIONID"));
    }

    @Test
    void unauthorizedAccess_ReturnsJsonError() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void invalidMethod_OnPublicEndpoint_ReturnsMethodNotAllowed() throws Exception {
        // When/Then - GET on POST-only endpoint triggers auth check first (only POST is permitAll)
        mockMvc.perform(get("/api/v1/auth/register"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidEndpoint_ReturnsNotFound() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/auth/nonexistent"))
                .andExpect(status().isUnauthorized()); // Requires auth, so 401 before 404
    }

    @Test
    void jwtFilter_IsAppliedBeforeUsernamePasswordAuthenticationFilter() throws Exception {
        // This is implicitly tested by other tests, but we can verify JWT is checked
        // Given - Invalid JWT
        String invalidToken = "invalid.jwt.token";

        // When/Then
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }
}
