package edu.cit.sala.TaskFlow.feature.file;

import edu.cit.sala.TaskFlow.feature.file.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByTaskId(Long taskId);

    List<FileEntity> findByUploadedById(Long userId);
}
