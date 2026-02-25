package com.innovatepam.auth.repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.innovatepam.auth.model.AuthenticationAttempt;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthenticationAttemptRepositoryIntegrationTest {

    @Autowired
    private AuthenticationAttemptRepository authenticationAttemptRepository;

    @BeforeEach
    void setUp() {
        authenticationAttemptRepository.deleteAll();
    }

    @Test
    void findByEmailAndAttemptTimeAfter_WithRecentFailedAttempts_ReturnsAttempts() {
        // Given
        String email = "test@example.com";
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        
        AuthenticationAttempt attempt1 = new AuthenticationAttempt();
        attempt1.setEmail(email);
        attempt1.setAttemptTime(LocalDateTime.now().minusMinutes(5));
        attempt1.setSuccess(false);
        attempt1.setIpAddress("192.168.1.1");
        
        AuthenticationAttempt attempt2 = new AuthenticationAttempt();
        attempt2.setEmail(email);
        attempt2.setAttemptTime(LocalDateTime.now().minusMinutes(10));
        attempt2.setSuccess(false);
        attempt2.setIpAddress("192.168.1.1");
        
        authenticationAttemptRepository.save(attempt1);
        authenticationAttemptRepository.save(attempt2);

        // When
        List<AuthenticationAttempt> attempts = authenticationAttemptRepository
                .findRecentFailedAttempts(email, fifteenMinutesAgo);

        // Then
        assertEquals(2, attempts.size());
    }

    @Test
    void findRecentFailedAttempts_WithOldAttempts_ReturnsEmpty() {
        // Given
        String email = "old@example.com";
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        
        AuthenticationAttempt attempt = new AuthenticationAttempt();
        attempt.setEmail(email);
        attempt.setAttemptTime(LocalDateTime.now().minusMinutes(20));
        attempt.setSuccess(false);
        attempt.setIpAddress("192.168.1.1");
        
        authenticationAttemptRepository.save(attempt);

        // When
        List<AuthenticationAttempt> attempts = authenticationAttemptRepository
                .findRecentFailedAttempts(email, fifteenMinutesAgo);

        // Then
        assertTrue(attempts.isEmpty());
    }

    @Test
    void findRecentFailedAttempts_WithMixedAttempts_ReturnsOnlyFailed() {
        // Given
        String email = "mixed@example.com";
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        
        AuthenticationAttempt failed1 = new AuthenticationAttempt();
        failed1.setEmail(email);
        failed1.setAttemptTime(LocalDateTime.now().minusMinutes(5));
        failed1.setSuccess(false);
        failed1.setIpAddress("192.168.1.1");
        
        AuthenticationAttempt success = new AuthenticationAttempt();
        success.setEmail(email);
        success.setAttemptTime(LocalDateTime.now().minusMinutes(7));
        success.setSuccess(true);
        success.setIpAddress("192.168.1.1");
        
        AuthenticationAttempt failed2 = new AuthenticationAttempt();
        failed2.setEmail(email);
        failed2.setAttemptTime(LocalDateTime.now().minusMinutes(10));
        failed2.setSuccess(false);
        failed2.setIpAddress("192.168.1.1");
        
        authenticationAttemptRepository.save(failed1);
        authenticationAttemptRepository.save(success);
        authenticationAttemptRepository.save(failed2);

        // When
        List<AuthenticationAttempt> attempts = authenticationAttemptRepository
                .findRecentFailedAttempts(email, fifteenMinutesAgo);

        // Then
        assertEquals(2, attempts.size());
        assertTrue(attempts.stream().noneMatch(AuthenticationAttempt::isSuccess));
    }

    @Test
    void save_WithValidAttempt_SavesSuccessfully() {
        // Given
        AuthenticationAttempt attempt = new AuthenticationAttempt();
        attempt.setEmail("save@example.com");
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setSuccess(false);
        attempt.setIpAddress("192.168.1.100");

        // When
        AuthenticationAttempt saved = authenticationAttemptRepository.save(attempt);

        // Then
        assertNotNull(saved.getId());
        assertEquals("save@example.com", saved.getEmail());
        assertEquals("192.168.1.100", saved.getIpAddress());
        assertFalse(saved.isSuccess());
    }

    @Test
    void findRecentFailedAttempts_WithMultipleFailures_ReturnsCorrectCount() {
        // Given
        String email = "count@example.com";
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        
        for (int i = 0; i < 5; i++) {
            AuthenticationAttempt attempt = new AuthenticationAttempt();
            attempt.setEmail(email);
            attempt.setAttemptTime(LocalDateTime.now().minusMinutes(i + 1));
            attempt.setSuccess(false);
            attempt.setIpAddress("192.168.1.1");
            authenticationAttemptRepository.save(attempt);
        }

        // When
        List<AuthenticationAttempt> attempts = authenticationAttemptRepository
                .findRecentFailedAttempts(email, fifteenMinutesAgo);

        // Then
        assertEquals(5, attempts.size());
    }

    @Test
    void findRecentFailedAttempts_WithDifferentEmails_ReturnsOnlyMatchingEmail() {
        // Given
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        
        AuthenticationAttempt attempt1 = new AuthenticationAttempt();
        attempt1.setEmail("user1@example.com");
        attempt1.setAttemptTime(LocalDateTime.now().minusMinutes(5));
        attempt1.setSuccess(false);
        attempt1.setIpAddress("192.168.1.1");
        
        AuthenticationAttempt attempt2 = new AuthenticationAttempt();
        attempt2.setEmail("user2@example.com");
        attempt2.setAttemptTime(LocalDateTime.now().minusMinutes(5));
        attempt2.setSuccess(false);
        attempt2.setIpAddress("192.168.1.2");
        
        authenticationAttemptRepository.save(attempt1);
        authenticationAttemptRepository.save(attempt2);

        // When
        List<AuthenticationAttempt> attempts = authenticationAttemptRepository
                .findRecentFailedAttempts("user1@example.com", fifteenMinutesAgo);

        // Then
        assertEquals(1, attempts.size());
        assertEquals("user1@example.com", attempts.get(0).getEmail());
    }
}
