package com.innovatepam.idea.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.innovatepam.idea.model.FileType;

class FileValidatorTest {
    
    @Test
    void testValidPdfFile() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "proposal.pdf",
            "application/pdf",
            new byte[1024]
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertTrue(result.isValid(), "Valid PDF should pass validation");
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void testValidPngFile() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "diagram.png",
            "image/png",
            new byte[2048]
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertTrue(result.isValid(), "Valid PNG should pass validation");
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void testFileTooLarge() {
        byte[] largeContent = new byte[(int) (FileValidator.MAX_FILE_SIZE + 1)];
        MultipartFile file = new MockMultipartFile(
            "file",
            "large.pdf",
            "application/pdf",
            largeContent
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertFalse(result.isValid(), "File exceeding 50MB should fail validation");
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("exceeds maximum"), "Error message should mention size limit");
    }
    
    @Test
    void testInvalidFileType() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "document.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            new byte[1024]
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertFalse(result.isValid(), "DOCX file should fail validation");
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("not allowed"), "Error message should mention invalid type");
    }
    
    @Test
    void testInvalidFileExtension() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.jpg",
            "image/jpeg",
            new byte[1024]
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertFalse(result.isValid(), "JPG file should fail validation");
    }
    
    @Test
    void testNullFile() {
        FileValidator.ValidationResult result = FileValidator.validate(null);
        
        assertFalse(result.isValid(), "Null file should fail validation");
        assertEquals("File is required", result.getErrorMessage());
    }
    
    @Test
    void testEmptyFile() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            new byte[0]
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertFalse(result.isValid(), "Empty file should fail validation");
        assertEquals("File is required", result.getErrorMessage());
    }
    
    @Test
    void testMissingFilename() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "",
            "application/pdf",
            new byte[1024]
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertFalse(result.isValid(), "File without filename should fail validation");
        assertEquals("Filename is required", result.getErrorMessage());
    }
    
    @Test
    void testGetFileTypePdf() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "doc.pdf",
            "application/pdf",
            new byte[1024]
        );
        
        FileType fileType = FileValidator.getFileType(file);
        
        assertEquals(FileType.PDF, fileType);
    }
    
    @Test
    void testGetFileTypePng() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.png",
            "image/png",
            new byte[1024]
        );
        
        FileType fileType = FileValidator.getFileType(file);
        
        assertEquals(FileType.PNG, fileType);
    }
    
    @Test
    void testGetFileTypeUnsupported() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "doc.txt",
            "text/plain",
            new byte[1024]
        );
        
        assertThrows(IllegalArgumentException.class, () -> {
            FileValidator.getFileType(file);
        }, "Unsupported file type should throw exception");
    }
    
    @Test
    void testGetFileExtension() {
        assertEquals("pdf", FileValidator.getFileExtension("document.pdf"));
        assertEquals("png", FileValidator.getFileExtension("image.png"));
        assertEquals("txt", FileValidator.getFileExtension("file.txt"));
    }
    
    @Test
    void testGetFileExtensionNoExtension() {
        assertEquals("", FileValidator.getFileExtension("noextension"));
        assertEquals("", FileValidator.getFileExtension(""));
        assertEquals("", FileValidator.getFileExtension(null));
    }
    
    @Test
    void testGetFileExtensionMultipleDots() {
        assertEquals("pdf", FileValidator.getFileExtension("my.file.name.pdf"));
    }
    
    @Test
    void testMaxFileSizeConstant() {
        assertEquals(52_428_800L, FileValidator.MAX_FILE_SIZE, "Max file size should be 50MB");
    }
    
    @Test
    void testFileExactlyAtMaxSize() {
        byte[] content = new byte[(int) FileValidator.MAX_FILE_SIZE];
        MultipartFile file = new MockMultipartFile(
            "file",
            "maxsize.pdf",
            "application/pdf",
            content
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertTrue(result.isValid(), "File exactly at max size should pass validation");
    }
    
    @Test
    void testCaseInsensitiveContentType() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "APPLICATION/PDF",
            new byte[1024]
        );
        
        FileValidator.ValidationResult result = FileValidator.validate(file);
        
        assertTrue(result.isValid(), "Content type check should be case-insensitive");
    }
}
