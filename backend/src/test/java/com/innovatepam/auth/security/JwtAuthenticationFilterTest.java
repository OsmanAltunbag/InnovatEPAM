package com.innovatepam.auth.security;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter authFilter;

    @BeforeEach
    void setUp() {
        authFilter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    private Claims createTestClaims(String email, String role) {
        Claims claims = mock(Claims.class, withSettings().lenient());
        when(claims.getSubject()).thenReturn(email);
        when(claims.get("role", String.class)).thenReturn(role);
        return claims;
    }

    @Test
    void doFilterInternal_WithValidBearerToken_SetsAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;
        Claims claims = createTestClaims("test@example.com", "submitter");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.parseToken(token)).thenReturn(claims);

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("test@example.com", auth.getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithoutAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithoutBearerPrefix_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).parseToken(anyString());
    }

    @Test
    void doFilterInternal_WithInvalidToken_ClearsSecurityContext() throws ServletException, IOException {
        // Given
        String token = "invalid-token";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.parseToken(token)).thenThrow(new RuntimeException("Invalid token"));

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExtractsRoleFromClaims() throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        String roleValue = "evaluator/admin";
        Claims claims = createTestClaims("test@example.com", roleValue);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.parseToken(token)).thenReturn(claims);

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should not be null");
        assertEquals("test@example.com", auth.getPrincipal(), "Principal should be email");
        
        String expectedAuthority = "ROLE_" + roleValue.toUpperCase(java.util.Locale.ROOT);
        assertTrue(auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(expectedAuthority)), 
            "Expected to find " + expectedAuthority + " in authorities");
    }

    @Test
    void doFilterInternal_WithWhitespaceInToken_TrimsToken() throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        Claims claims = createTestClaims("test@example.com", "submitter");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token + "  ");
        when(jwtService.parseToken(token)).thenReturn(claims);

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).parseToken(token);
    }

    @Test
    void doFilterInternal_WithDifferentRoles_SetsCorrectAuthority() throws ServletException, IOException {
        // Given
        String[] roles = {"submitter", "evaluator/admin"};

        for (String role : roles) {
            String token = "token-for-" + role;
            Claims claims = createTestClaims("test@example.com", role);

            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
            when(jwtService.parseToken(token)).thenReturn(claims);

            SecurityContextHolder.clearContext();

            // When
            authFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String expectedAuthority = "ROLE_" + role.toUpperCase(java.util.Locale.ROOT);
            assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(expectedAuthority)), 
                "Expected to find " + expectedAuthority + " in authorities for role: " + role);
        }
    }

    @Test
    void doFilterInternal_AlwaysContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithExpiredToken_ClearsSecurityContext() throws ServletException, IOException {
        // Given
        String token = "expired-token";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.parseToken(token)).thenThrow(new RuntimeException("Token expired"));

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_UsesSubjectAsEmailPrincipal() throws ServletException, IOException {
        // Given
        Claims claims = createTestClaims("user@example.com", "submitter");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(jwtService.parseToken("valid-token")).thenReturn(claims);

        // When
        authFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("user@example.com", auth.getPrincipal());
    }
}
