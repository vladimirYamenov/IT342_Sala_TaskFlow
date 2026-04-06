package edu.cit.sala.TaskFlow.controller;

import edu.cit.sala.TaskFlow.dto.FileResponse;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "taskId", required = false) Long taskId,
                                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        FileResponse response = fileService.uploadFile(file, taskId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getMyFiles(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(fileService.getFilesByUser(user.getId()));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<FileResponse>> getTaskFiles(@PathVariable Long taskId) {
        return ResponseEntity.ok(fileService.getFilesByTask(taskId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Resource resource = fileService.downloadFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        fileService.deleteFile(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
