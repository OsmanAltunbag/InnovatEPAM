package com.innovatepam.idea.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.innovatepam.auth.AuthApplication;
import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;
import com.innovatepam.auth.repository.RoleRepository;
import com.innovatepam.auth.repository.UserRepository;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest(classes = AuthApplication.class)
@Testcontainers
@Transactional
class IdeaRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User submitter;
    private Role submitterRole;

    @BeforeEach
    void setUp() {
        submitterRole = new Role();
        submitterRole.setId(UUID.randomUUID());
        submitterRole.setName("SUBMITTER");
        submitterRole.setCreatedAt(LocalDateTime.now());
        submitterRole = roleRepository.save(submitterRole);

        submitter = new User();
        submitter.setId(UUID.randomUUID());
        submitter.setEmail("submitter@test.com");
        submitter.setPasswordHash("hashed_password");
        submitter.setRole(submitterRole);
        submitter.setCreatedAt(LocalDateTime.now());
        submitter = userRepository.save(submitter);
    }

    @Test
    void testSaveAndFindIdea() {
        Idea idea = new Idea();
        idea.setTitle("Test Idea");
        idea.setDescription("Test Description");
        idea.setCategory("Process Improvement");
        idea.setSubmitter(submitter);
        idea.onCreate();

        Idea saved = ideaRepository.save(idea);

        assertNotNull(saved.getId());
        assertEquals("Test Idea", saved.getTitle());
        assertEquals(IdeaStatus.SUBMITTED, saved.getStatus());

        Optional<Idea> found = ideaRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Idea", found.get().getTitle());
    }

    @Test
    void testFindByStatus() {
        // Create ideas with different statuses
        Idea submittedIdea = createIdea("Submitted Idea", IdeaStatus.SUBMITTED);
        Idea reviewIdea = createIdea("Review Idea", IdeaStatus.UNDER_REVIEW);

        ideaRepository.save(submittedIdea);
        ideaRepository.save(reviewIdea);

        Page<Idea> submittedPage = ideaRepository.findByStatus(
            IdeaStatus.SUBMITTED, 
            PageRequest.of(0, 10)
        );

        assertEquals(1, submittedPage.getContent().size());
        assertEquals("Submitted Idea", submittedPage.getContent().get(0).getTitle());

        Page<Idea> reviewPage = ideaRepository.findByStatus(
            IdeaStatus.UNDER_REVIEW, 
            PageRequest.of(0, 10)
        );

        assertEquals(1, reviewPage.getContent().size());
        assertEquals("Review Idea", reviewPage.getContent().get(0).getTitle());
    }

    @Test
    void testFindBySubmitterId() {
        Idea idea1 = createIdea("Idea 1", IdeaStatus.SUBMITTED);
        Idea idea2 = createIdea("Idea 2", IdeaStatus.UNDER_REVIEW);

        ideaRepository.save(idea1);
        ideaRepository.save(idea2);

        Page<Idea> userIdeas = ideaRepository.findBySubmitterId(
            submitter.getId(), 
            PageRequest.of(0, 10)
        );

        assertEquals(2, userIdeas.getContent().size());
        assertTrue(userIdeas.getContent().stream()
            .allMatch(idea -> idea.getSubmitter().getId().equals(submitter.getId())));
    }

    @Test
    void testFindAllWithPagination() {
        for (int i = 1; i <= 15; i++) {
            Idea idea = createIdea("Idea " + i, IdeaStatus.SUBMITTED);
            ideaRepository.save(idea);
        }

        Page<Idea> firstPage = ideaRepository.findAll(PageRequest.of(0, 10));
        assertEquals(10, firstPage.getContent().size());
        assertEquals(15, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());

        Page<Idea> secondPage = ideaRepository.findAll(PageRequest.of(1, 10));
        assertEquals(5, secondPage.getContent().size());
    }

    @Test
    void testUpdateIdeaStatus() {
        Idea idea = createIdea("Test Idea", IdeaStatus.SUBMITTED);
        Idea saved = ideaRepository.save(idea);

        saved.setStatus(IdeaStatus.UNDER_REVIEW);
        saved.onUpdate();
        ideaRepository.save(saved);

        Optional<Idea> updated = ideaRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals(IdeaStatus.UNDER_REVIEW, updated.get().getStatus());
        assertNotEquals(updated.get().getCreatedAt(), updated.get().getUpdatedAt());
    }

    @Test
    void testDeleteIdea() {
        Idea idea = createIdea("Test Idea", IdeaStatus.SUBMITTED);
        Idea saved = ideaRepository.save(idea);
        Long ideaId = saved.getId();

        ideaRepository.deleteById(ideaId);

        Optional<Idea> deleted = ideaRepository.findById(ideaId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testOptimisticLocking() {
        Idea idea = createIdea("Test Idea", IdeaStatus.SUBMITTED);
        Idea saved = ideaRepository.saveAndFlush(idea);
        
        // Detach the entity from the persistence context
        entityManager.detach(saved);

        // Simulate concurrent modification
        Idea idea1 = ideaRepository.findById(saved.getId()).get();
        entityManager.detach(idea1);
        
        Idea idea2 = ideaRepository.findById(saved.getId()).get();
        entityManager.detach(idea2);

        idea1.setTitle("Updated by User 1");
        ideaRepository.saveAndFlush(idea1);

        // This should fail due to version mismatch
        idea2.setTitle("Updated by User 2");
        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
            ideaRepository.saveAndFlush(idea2)
        );
    }

    private Idea createIdea(String title, IdeaStatus status) {
        Idea idea = new Idea();
        idea.setTitle(title);
        idea.setDescription("Description for " + title);
        idea.setCategory("Process Improvement");
        idea.setSubmitter(submitter);
        idea.setStatus(status);
        idea.onCreate();
        return idea;
    }
}
