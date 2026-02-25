package com.innovatepam.auth.service;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private RoleRepository roleRepository;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository);
    }

    private Role createTestRole(String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        return role;
    }

    @Test
    void getRoleByName_WithValidSubmitterRole_ReturnsRole() {
        // Given
        Role submitterRole = createTestRole("submitter");
        when(roleRepository.findByName("submitter")).thenReturn(Optional.of(submitterRole));

        // When
        Role result = roleService.getRoleByName("submitter");

        // Then
        assertNotNull(result);
        assertEquals("submitter", result.getName());
        verify(roleRepository).findByName("submitter");
    }

    @Test
    void getRoleByName_WithValidEvaluatorAdminRole_ReturnsRole() {
        // Given
        Role adminRole = createTestRole("evaluator/admin");
        when(roleRepository.findByName("evaluator/admin")).thenReturn(Optional.of(adminRole));

        // When
        Role result = roleService.getRoleByName("evaluator/admin");

        // Then
        assertNotNull(result);
        assertEquals("evaluator/admin", result.getName());
        verify(roleRepository).findByName("evaluator/admin");
    }

    @Test
    void getRoleByName_WithInvalidRole_ThrowsResponseStatusException() {
        // Given
        when(roleRepository.findByName("invalid")).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResponseStatusException.class, () -> roleService.getRoleByName("invalid"));
        verify(roleRepository).findByName("invalid");
    }

    @Test
    void getRoleByName_WithNullInput_ThrowsResponseStatusException() {
        // Given
        when(roleRepository.findByName("")).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResponseStatusException.class, () -> roleService.getRoleByName(null));
        verify(roleRepository).findByName("");
    }

    @Test
    void getRoleByName_NormalizesInputToLowercase() {
        // Given
        Role submitterRole = createTestRole("submitter");
        when(roleRepository.findByName("submitter")).thenReturn(Optional.of(submitterRole));

        // When
        roleService.getRoleByName("SUBMITTER");

        // Then
        verify(roleRepository).findByName("submitter");
    }

    @Test
    void getRoleByName_TrimsWhitespaceFromInput() {
        // Given
        Role submitterRole = createTestRole("submitter");
        when(roleRepository.findByName("submitter")).thenReturn(Optional.of(submitterRole));

        // When
        roleService.getRoleByName("  submitter  ");

        // Then
        verify(roleRepository).findByName("submitter");
    }

    @Test
    void getRoleByName_HandlesNormalizationWithMixedCase() {
        // Given
        Role adminRole = createTestRole("evaluator/admin");
        when(roleRepository.findByName("evaluator/admin")).thenReturn(Optional.of(adminRole));

        // When
        roleService.getRoleByName("EVALUATOR/ADMIN");

        // Then
        verify(roleRepository).findByName("evaluator/admin");
    }

    @Test
    void getRoleByName_ExceptionMessageIsInformative() {
        // Given
        when(roleRepository.findByName("unknown")).thenReturn(Optional.empty());

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> roleService.getRoleByName("unknown"));

        // Then
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Invalid role"));
    }

    @Test
    void getRoleByName_RepositoryCalledOnce() {
        // Given
        Role submitterRole = createTestRole("submitter");
        when(roleRepository.findByName("submitter")).thenReturn(Optional.of(submitterRole));

        // When
        roleService.getRoleByName("submitter");

        // Then
        verify(roleRepository, times(1)).findByName("submitter");
    }

    @Test
    void getRoleByName_DoesNotModifyRoleData() {
        // Given
        Role originalRole = createTestRole("submitter");
        when(roleRepository.findByName("submitter")).thenReturn(Optional.of(originalRole));

        // When
        Role result = roleService.getRoleByName("submitter");

        // Then
        assertEquals(originalRole.getId(), result.getId());
        assertEquals(originalRole.getName(), result.getName());
    }
}
