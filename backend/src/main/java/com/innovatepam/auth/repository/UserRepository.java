package com.innovatepam.auth.repository;

import com.innovatepam.auth.model.User;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select u from User u where u.email = :email and u.locked = false")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    @Query("select u from User u where u.email = :email and u.lockedUntil is not null and u.lockedUntil > :now")
    Optional<User> findLockedUser(@Param("email") String email, @Param("now") LocalDateTime now);
}
