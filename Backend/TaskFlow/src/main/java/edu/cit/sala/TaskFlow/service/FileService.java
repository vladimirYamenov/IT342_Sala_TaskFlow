package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.dto.FileResponse;
import edu.cit.sala.TaskFlow.entity.FileEntity;
import edu.cit.sala.TaskFlow.entity.Task;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.repository.FileRepository;
import edu.cit.sala.TaskFlow.repository.TaskRepository;
import edu.cit.sala.TaskFlow.service.storage.StorageAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Refactored to use the Adapter Pattern via StorageAdapter.
 *
 * Before: FileService directly used java.nio.file APIs (Paths, Files, UrlResource)
 * for local filesystem operations. This tightly coupled the service to local storage.
 *
 * After: StorageAdapter interface abstracts storage operations. LocalStorageAdapter
 * handles the actual filesystem calls. To switch to cloud storage (S3, GCS),
 * only a new adapter implementation is needed — FileService remains unchanged.
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final TaskRepository taskRepository;
    private final StorageAdapter storageAdapter;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf"
    );

    public FileResponse uploadFile(MultipartFile file, Long taskId, User user) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images and PDFs are allowed");
        }

        Task task = null;
        if (taskId != null) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        }

        try {
            String originalName = file.getOriginalFilename();
            String storedName = storageAdapter.store(originalName, file.getInputStream(), file.getSize());

            FileEntity fileEntity = FileEntity.builder()
                    .fileName(originalName != null ? originalName : storedName)
                    .fileType(contentType)
                    .filePath(storedName)
                    .fileSize(file.getSize())
                    .task(task)
                    .uploadedBy(user)
                    .build();

            FileEntity saved = fileRepository.save(fileEntity);
            return toResponse(saved);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }

    public List<FileResponse> getFilesByTask(Long taskId) {
        return fileRepository.findByTaskId(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<FileResponse> getFilesByUser(Long userId) {
        return fileRepository.findByUploadedById(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Resource downloadFile(Long fileId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        return storageAdapter.load(fileEntity.getFilePath());
    }

    public void deleteFile(Long fileId, Long userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        if (!fileEntity.getUploadedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        storageAdapter.delete(fileEntity.getFilePath());
        fileRepository.delete(fileEntity);
    }

    private FileResponse toResponse(FileEntity entity) {
        return FileResponse.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .uploadedBy(entity.getUploadedBy().getId())
                .uploadedAt(entity.getUploadedAt())
                .downloadUrl("/api/files/" + entity.getId() + "/download")
                .build();
    }
}
