package com.innovatepam.auth.repository;

import com.innovatepam.auth.model.AuthenticationAttempt;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthenticationAttemptRepository extends JpaRepository<AuthenticationAttempt, UUID> {
    @Query("select a from AuthenticationAttempt a where a.email = :email and a.success = false and a.attemptTime > :since")
    List<AuthenticationAttempt> findRecentFailedAttempts(
        @Param("email") String email,
        @Param("since") LocalDateTime since
    );
}
