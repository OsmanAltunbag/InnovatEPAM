package com.innovatepam.auth.service;

import com.innovatepam.auth.model.AuthenticationAttempt;
import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.AuthenticationAttemptRepository;
import com.innovatepam.auth.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int WINDOW_MINUTES = 15;
    private static final int LOCK_MINUTES = 30;

    private final AuthenticationAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    public AuthenticationAttemptService(
        AuthenticationAttemptRepository attemptRepository,
        UserRepository userRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
    }

    public void recordSuccess(String email, String ipAddress) {
        AuthenticationAttempt attempt = new AuthenticationAttempt();
        attempt.setEmail(email);
        attempt.setSuccess(true);
        attempt.setIpAddress(ipAddress);
        attemptRepository.save(attempt);
    }

    public boolean recordFailure(User user, String ipAddress) {
        AuthenticationAttempt attempt = new AuthenticationAttempt();
        attempt.setEmail(user.getEmail());
        attempt.setSuccess(false);
        attempt.setIpAddress(ipAddress);
        attemptRepository.save(attempt);

        LocalDateTime since = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        List<AuthenticationAttempt> recentFailures = attemptRepository.findRecentFailedAttempts(user.getEmail(), since);
        if (recentFailures.size() >= MAX_ATTEMPTS) {
            user.setLocked(true);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
