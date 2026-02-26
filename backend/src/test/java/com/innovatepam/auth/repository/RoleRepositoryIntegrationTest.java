package com.innovatepam.auth.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.innovatepam.auth.model.Role;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_WithSubmitterRole_ReturnsRole() {
        // When
        Optional<Role> role = roleRepository.findByName("submitter");

        // Then
        assertTrue(role.isPresent());
        assertEquals("submitter", role.get().getName());
        assertNotNull(role.get().getId());
    }

    @Test
    void findByName_WithEvaluatorAdminRole_ReturnsRole() {
        // When
        Optional<Role> role = roleRepository.findByName("evaluator/admin");

        // Then
        assertTrue(role.isPresent());
        assertEquals("evaluator/admin", role.get().getName());
        assertNotNull(role.get().getId());
    }

    @Test
    void findByName_WithNonExistingRole_ReturnsEmpty() {
        // When
        Optional<Role> role = roleRepository.findByName("nonexistent");

        // Then
        assertFalse(role.isPresent());
    }

    @Test
    void findByName_WithCaseSensitivity_ReturnsEmpty() {
        // When
        Optional<Role> role = roleRepository.findByName("SUBMITTER");

        // Then
        assertFalse(role.isPresent(), "Role lookup should be case-sensitive");
    }
}
