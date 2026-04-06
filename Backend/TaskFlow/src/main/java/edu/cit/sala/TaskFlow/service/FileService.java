package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.dto.FileResponse;
import edu.cit.sala.TaskFlow.entity.FileEntity;
import edu.cit.sala.TaskFlow.entity.Task;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.repository.FileRepository;
import edu.cit.sala.TaskFlow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final TaskRepository taskRepository;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

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
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + extension;

            Path targetPath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

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

        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileEntity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk");
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file");
        }
    }

    public void deleteFile(Long fileId, Long userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        if (!fileEntity.getUploadedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileEntity.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't fail — DB record will still be removed
        }

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
