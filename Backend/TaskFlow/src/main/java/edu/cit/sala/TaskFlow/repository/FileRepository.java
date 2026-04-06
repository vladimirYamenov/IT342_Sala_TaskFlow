package edu.cit.sala.TaskFlow.repository;

import edu.cit.sala.TaskFlow.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByTaskId(Long taskId);

    List<FileEntity> findByUploadedById(Long userId);
}
