package com.innovatepam.idea.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.innovatepam.auth.model.Role;
import com.innovatepam.auth.model.User;
import com.innovatepam.idea.exception.InvalidFileException;
import com.innovatepam.idea.model.FileType;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaAttachment;
import com.innovatepam.idea.model.IdeaStatus;
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    private User submitter;
    private Idea idea;
    private Role submitterRole;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString(), 50L * 1024L * 1024L);

        submitterRole = new Role();
        submitterRole.setId(UUID.randomUUID());
        submitterRole.setName("SUBMITTER");
        submitterRole.setCreatedAt(LocalDateTime.now());

        submitter = new User();
        submitter.setId(UUID.randomUUID());
        submitter.setEmail("submitter@example.com");
        submitter.setPasswordHash("hashed_password");
        submitter.setRole(submitterRole);
        submitter.setCreatedAt(LocalDateTime.now());

        idea = new Idea();
        idea.setId(1L);
        idea.setTitle("Test Idea");
        idea.setDescription("Description");
        idea.setCategory("Process Improvement");
        idea.setSubmitter(submitter);
        idea.setStatus(IdeaStatus.SUBMITTED);
        idea.onCreate();
    }

    @Test
    void testStoreValidPdfFile() throws IOException {
        MultipartFile file = new MockMultipartFile(
            "file",
            "proposal.pdf",
            "application/pdf",
            "PDF content".getBytes()
        );

        IdeaAttachment result = fileStorageService.storeFile(file, idea);

        assertNotNull(result);
        assertEquals("proposal.pdf", result.getOriginalFilename());
        assertEquals(FileType.PDF, result.getFileType());
        assertEquals("PDF content".getBytes().length, result.getFileSize());
        assertNotNull(result.getStoredFilename());
        assertNotNull(result.getStorageLocation());

        // Verify file was actually written to disk
        Path storedFile = tempDir.resolve(idea.getId().toString()).resolve(result.getStoredFilename());
        assertTrue(Files.exists(storedFile), "File should be written to storage");
    }

    @Test
    void testStoreValidPngFile() throws IOException {
        MultipartFile file = new MockMultipartFile(
            "file",
            "diagram.png",
            "image/png",
            "PNG content".getBytes()
        );

        IdeaAttachment result = fileStorageService.storeFile(file, idea);

        assertNotNull(result);
        assertEquals("diagram.png", result.getOriginalFilename());
        assertEquals(FileType.PNG, result.getFileType());

    }

    @Test
    void testStoreFileTooBig() {
        byte[] largeContent = new byte[51 * 1024 * 1024]; // 51MB
        MultipartFile file = new MockMultipartFile(
            "file",
            "large.pdf",
            "application/pdf",
            largeContent
        );

        assertThrows(InvalidFileException.class, () -> {
            fileStorageService.storeFile(file, idea);
        });
    }

    @Test
    void testStoreInvalidFileType() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "document.txt",
            "text/plain",
            "Text content".getBytes()
        );

        assertThrows(InvalidFileException.class, () -> {
            fileStorageService.storeFile(file, idea);
        });
    }

    @Test
    void testStoreNullFile() {
        assertThrows(InvalidFileException.class, () -> {
            fileStorageService.storeFile(null, idea);
        });
    }

    @Test
    void testStoreEmptyFile() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            new byte[0]
        );

        assertThrows(InvalidFileException.class, () -> {
            fileStorageService.storeFile(file, idea);
        });
    }

    @Test
    void testStoredFilenameIsUnique() throws IOException {
        MultipartFile file = new MockMultipartFile(
            "file",
            "proposal.pdf",
            "application/pdf",
            "Content".getBytes()
        );

        IdeaAttachment result1 = fileStorageService.storeFile(file, idea);

        // Store same filename again for different idea
        Idea idea2 = new Idea();
        idea2.setId(2L);
        idea2.setTitle("Another Idea");
        idea2.setDescription("Description");
        idea2.setCategory("Innovation");
        idea2.setSubmitter(submitter);
        idea2.onCreate();

        IdeaAttachment result2 = fileStorageService.storeFile(file, idea2);

        assertNotEquals(result1.getStoredFilename(), result2.getStoredFilename(),
            "Stored filenames should be unique even for same original filename");
    }

    @Test
    void testStorageLocationContainsIdeaId() throws IOException {
        MultipartFile file = new MockMultipartFile(
            "file",
            "proposal.pdf",
            "application/pdf",
            "Content".getBytes()
        );

        IdeaAttachment result = fileStorageService.storeFile(file, idea);

        assertTrue(result.getStorageLocation().contains("1"),
            "Storage location should contain idea ID");
    }

    @Test
    void testFileTypeValidation() throws IOException {
        MultipartFile pdfFile = new MockMultipartFile(
            "file",
            "doc.pdf",
            "application/pdf",
            "PDF".getBytes()
        );

        IdeaAttachment result = fileStorageService.storeFile(pdfFile, idea);
        assertEquals(FileType.PDF, result.getFileType());

        MultipartFile pngFile = new MockMultipartFile(
            "file",
            "image.png",
            "image/png",
            "PNG".getBytes()
        );

        result = fileStorageService.storeFile(pngFile, idea);
        assertEquals(FileType.PNG, result.getFileType());
    }

    @Test
    void testAttachmentSavedWithCorrectMetadata() throws IOException {
        MultipartFile file = new MockMultipartFile(
            "file",
            "test-file.pdf",
            "application/pdf",
            "Test content for file size".getBytes()
        );

        IdeaAttachment saved = fileStorageService.storeFile(file, idea);

        assertEquals(idea, saved.getIdea());
        assertEquals("test-file.pdf", saved.getOriginalFilename());
        assertEquals(FileType.PDF, saved.getFileType());
        assertEquals((long) file.getSize(), saved.getFileSize());
        assertNotNull(saved.getStoredFilename());
        assertNotNull(saved.getStorageLocation());
    }
}
