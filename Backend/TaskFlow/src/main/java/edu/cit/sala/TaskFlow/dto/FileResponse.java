package edu.cit.sala.TaskFlow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long taskId;
    private Long uploadedBy;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
