package com.innovatepam.idea.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.web.multipart.MultipartFile;

import com.innovatepam.idea.model.FileType;

/**
 * Validates file uploads for idea attachments.
 * 
 * Enforces:
 * - Allowed file types: PDF, PNG
 * - Maximum file size: 50MB (52,428,800 bytes)
 */
public class FileValidator {
    
    public static final long MAX_FILE_SIZE = 52_428_800L; // 50MB in bytes
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "application/pdf",
        "image/png"
    );
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "png");
    
    /**
     * Validates if the uploaded file meets all requirements.
     * 
     * @param file The uploaded file to validate
     * @return ValidationResult containing validation status and error message if any
     */
    public static ValidationResult validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.invalid("File is required");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return ValidationResult.invalid(
                String.format("File size exceeds maximum allowed size of 50MB (received: %.2f MB)", 
                    file.getSize() / 1024.0 / 1024.0)
            );
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            return ValidationResult.invalid(
                String.format("File type '%s' is not allowed. Allowed types: PDF, PNG", contentType)
            );
        }
        
        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ValidationResult.invalid("Filename is required");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return ValidationResult.invalid(
                String.format("File extension '.%s' is not allowed. Allowed extensions: pdf, png", extension)
            );
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Determines the FileType enum from a MultipartFile based on content type.
     * 
     * @param file The uploaded file
     * @return FileType enum (PDF or PNG)
     * @throws IllegalArgumentException if file type is not supported
     */
    public static FileType getFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is null");
        }
        
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "application/pdf" -> FileType.PDF;
            case "image/png" -> FileType.PNG;
            default -> throw new IllegalArgumentException("Unsupported file type: " + contentType);
        };
    }
    
    /**
     * Extracts the file extension from a filename.
     * 
     * @param filename The filename
     * @return The file extension (without dot) or empty string if no extension
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * Result of file validation containing status and optional error message.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
