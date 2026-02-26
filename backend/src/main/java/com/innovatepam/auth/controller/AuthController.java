package com.innovatepam.auth.controller;

import com.innovatepam.auth.dto.AuthResponse;
import com.innovatepam.auth.dto.LoginRequest;
import com.innovatepam.auth.dto.RegisterRequest;
import com.innovatepam.auth.dto.UserInfoResponse;
import com.innovatepam.auth.service.AuthService;
import com.innovatepam.auth.service.RegistrationService;
import com.innovatepam.auth.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegistrationService registrationService;
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
        RegistrationService registrationService,
        AuthService authService,
        JwtService jwtService
    ) {
        this.registrationService = registrationService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        AuthResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is missing");
        }

        String token = header.substring("Bearer ".length()).trim();
        Claims claims = jwtService.parseToken(token);

        UserInfoResponse response = new UserInfoResponse(
            claims.get("userId", String.class),
            claims.getSubject(),
            claims.get("role", String.class),
            claims.get("createdAt", String.class)
        );
        return ResponseEntity.ok(response);
    }
}
