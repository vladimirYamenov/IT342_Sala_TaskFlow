package edu.cit.sala.TaskFlow.service.storage;

import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Adapter Pattern - Target interface.
 * Abstracts file storage operations so the storage backend can be swapped
 * (e.g., local filesystem, AWS S3, Google Cloud Storage) without
 * changing the FileService business logic.
 */
public interface StorageAdapter {

    /**
     * Store a file and return the stored file name/key.
     */
    String store(String originalFilename, InputStream inputStream, long size);

    /**
     * Load a file as a Resource for download.
     */
    Resource load(String storedFilename);

    /**
     * Delete a stored file. Returns true if deleted, false if not found.
     */
    boolean delete(String storedFilename);
}
