package com.innovatepam.idea.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.innovatepam.idea.exception.InvalidFileException;
import com.innovatepam.idea.model.FileType;
import com.innovatepam.idea.model.Idea;
import com.innovatepam.idea.model.IdeaAttachment;

@Service
public class FileStorageService {
    private static final long DEFAULT_MAX_FILE_SIZE = 50L * 1024L * 1024L;
    private static final String PDF_MIME = "application/pdf";
    private static final String PNG_MIME = "image/png";

    private final String uploadDirectory;
    private final long maxFileSize;

    public FileStorageService(
        @Value("${idea.upload.directory:uploads/ideas}") String uploadDirectory,
        @Value("${idea.upload.max-file-size:" + DEFAULT_MAX_FILE_SIZE + "}") long maxFileSize
    ) {
        this.uploadDirectory = uploadDirectory;
        this.maxFileSize = maxFileSize;
    }

    public IdeaAttachment storeFile(MultipartFile file, Idea idea) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }
        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException("File size exceeds the maximum allowed limit");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        validateContentType(file.getContentType(), extension);

        String storedFilename = UUID.randomUUID() + extension;
        Path ideaDirectory = Paths.get(uploadDirectory, idea.getId().toString());
        Path filePath = ideaDirectory.resolve(storedFilename);

        try {
            Files.createDirectories(ideaDirectory);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new InvalidFileException("Failed to store file", exception);
        }

        IdeaAttachment attachment = new IdeaAttachment();
        attachment.setIdea(idea);
        attachment.setOriginalFilename(originalFilename);
        attachment.setStoredFilename(storedFilename);
        attachment.setFileType(resolveFileType(extension));
        attachment.setFileSize(file.getSize());
        attachment.setStorageLocation(filePath.toString());

        return attachment;
    }

    public void deleteFile(IdeaAttachment attachment) {
        if (attachment == null || attachment.getStorageLocation() == null) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(attachment.getStorageLocation()));
        } catch (IOException exception) {
            throw new InvalidFileException("Failed to delete file", exception);
        }
    }

    public Resource loadFileAsResource(IdeaAttachment attachment) {
        if (attachment == null || attachment.getStorageLocation() == null) {
            throw new InvalidFileException("Attachment not found");
        }
        try {
            Path filePath = Paths.get(attachment.getStorageLocation()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new InvalidFileException("File not found or not readable");
            }
        } catch (MalformedURLException exception) {
            throw new InvalidFileException("Invalid file path", exception);
        }
    }

    private void validateContentType(String contentType, String extension) {
        String normalized = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        boolean validContentType = PDF_MIME.equals(normalized) || PNG_MIME.equals(normalized);
        boolean validExtension = ".pdf".equals(extension) || ".png".equals(extension);

        if (!validContentType || !validExtension) {
            throw new InvalidFileException("Only PDF and PNG files are allowed");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            throw new InvalidFileException("Filename is required");
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            throw new InvalidFileException("File extension is required");
        }
        return filename.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private FileType resolveFileType(String extension) {
        if (".pdf".equals(extension)) {
            return FileType.PDF;
        }
        if (".png".equals(extension)) {
            return FileType.PNG;
        }
        throw new InvalidFileException("Unsupported file type");
    }
}
