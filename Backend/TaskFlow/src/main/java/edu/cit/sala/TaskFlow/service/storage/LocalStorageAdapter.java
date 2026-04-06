package edu.cit.sala.TaskFlow.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Adapter Pattern - Concrete adapter for local filesystem storage.
 * Adapts the local filesystem API to the StorageAdapter interface.
 *
 * To switch to cloud storage (e.g., S3), create a new adapter implementing
 * StorageAdapter and swap the @Component annotation or use @Profile.
 */
@Component
public class LocalStorageAdapter implements StorageAdapter {

    private final Path uploadPath;

    public LocalStorageAdapter(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String store(String originalFilename, InputStream inputStream, long size) {
        try {
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + extension;

            Path targetPath = uploadPath.resolve(storedName);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return storedName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }

    @Override
    public Resource load(String storedFilename) {
        try {
            Path filePath = uploadPath.resolve(storedFilename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk");
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file");
        }
    }

    @Override
    public boolean delete(String storedFilename) {
        try {
            Path filePath = uploadPath.resolve(storedFilename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}
