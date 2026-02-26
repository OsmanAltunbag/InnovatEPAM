package com.innovatepam.auth.service;

import com.innovatepam.auth.dto.AuthResponse;
import com.innovatepam.auth.dto.LoginRequest;
import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.UserRepository;
import com.innovatepam.auth.security.JwtService;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationAttemptService attemptService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationAttemptService attemptService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.attemptService = attemptService;
    }

    public AuthResponse login(LoginRequest request, String ipAddress) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (user.isAccountLocked()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Account locked due to too many failed attempts. Try again after " + user.getLockedUntil()
            );
        }

        boolean matches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!matches) {
            boolean locked = attemptService.recordFailure(user, ipAddress);
            if (locked) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Account locked due to too many failed attempts. Try again after " + user.getLockedUntil()
                );
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        user.setLocked(false);
        user.setLockedUntil(null);
        userRepository.save(user);

        attemptService.recordSuccess(user.getEmail(), ipAddress);

        String token = jwtService.generateToken(user);
        return new AuthResponse(
            token,
            user.getEmail(),
            user.getRole().getName(),
            user.getId(),
            jwtService.getExpirationSeconds()
        );
    }
}
