package com.innovatepam.auth.security;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();
        try {
            Claims claims = jwtService.parseToken(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            // Sanitize role name: remove invalid characters and convert to uppercase
            // This ensures Spring Security authority names are always valid (e.g., ROLE_ADMIN, ROLE_EVALUATOR)
            String sanitizedRole = sanitizeRoleName(role);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + sanitizedRole))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Sanitize role names to comply with Spring Security authority naming rules.
     * Removes invalid characters (/, spaces, hyphens, etc.) and converts to uppercase.
     * 
     * Examples:
     * - "submitter" → "SUBMITTER"
     * - "admin" → "ADMIN"
     * - "evaluator/admin" → "EVALUATOR_ADMIN" (legacy format, replaced with "/" → "_")
     * - "user-role" → "USER_ROLE"
     * 
     * @param roleName Raw role name from JWT or database
     * @return Sanitized role name suitable for Spring Security authority
     */
    private String sanitizeRoleName(String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            return "USER";
        }
        // Remove invalid characters and convert to uppercase
        // Valid characters: A-Z, 0-9, underscore
        return roleName.toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9_]", "_")
            .replaceAll("_+", "_") // Collapse multiple underscores into one
            .replaceAll("^_|_$", ""); // Remove leading/trailing underscores
    }
}
