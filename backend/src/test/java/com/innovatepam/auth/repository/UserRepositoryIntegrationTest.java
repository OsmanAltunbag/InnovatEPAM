package com.innovatepam.auth.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role submitterRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        submitterRole = roleRepository.findByName("submitter")
                .orElseThrow(() -> new RuntimeException("Submitter role not found in test data"));
    }

    @Test
    void findByEmail_WithExistingUser_ReturnsUser() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole(submitterRole);
        user.setLocked(false);
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_WithNonExistingUser_ReturnsEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void existsByEmail_WithExistingUser_ReturnsTrue() {
        // Given
        User user = new User();
        user.setEmail("existing@example.com");
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole(submitterRole);
        user.setLocked(false);
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("existing@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_WithNonExistingUser_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void save_WithUniqueEmail_SavesSuccessfully() {
        // Given
        User user = new User();
        user.setEmail("unique@example.com");
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole(submitterRole);
        user.setLocked(false);

        // When
        User saved = userRepository.save(user);

        // Then
        assertNotNull(saved.getId());
        assertEquals("unique@example.com", saved.getEmail());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void save_WithDuplicateEmail_ThrowsException() {
        // Given
        User user1 = new User();
        user1.setEmail("duplicate@example.com");
        user1.setPasswordHash("$2a$12$hashedpassword");
        user1.setRole(submitterRole);
        user1.setLocked(false);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("duplicate@example.com");
        user2.setPasswordHash("$2a$12$anotherpassword");
        user2.setRole(submitterRole);
        user2.setLocked(false);

        // When/Then
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            userRepository.flush();
        });
    }

    @Test
    void findById_WithExistingUser_ReturnsUser() {
        // Given
        User user = new User();
        user.setEmail("findbyid@example.com");
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole(submitterRole);
        user.setLocked(false);
        User saved = userRepository.save(user);

        // When
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void delete_WithExistingUser_DeletesSuccessfully() {
        // Given
        User user = new User();
        user.setEmail("delete@example.com");
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole(submitterRole);
        user.setLocked(false);
        User saved = userRepository.save(user);

        // When
        userRepository.delete(saved);

        // Then
        assertFalse(userRepository.existsByEmail("delete@example.com"));
    }
}
