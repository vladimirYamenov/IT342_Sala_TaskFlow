package edu.cit.sala.TaskFlow.repository;

import edu.cit.sala.TaskFlow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Task> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    List<Task> findByUserIdAndPriorityOrderByCreatedAtDesc(Long userId, String priority);

    List<Task> findByUserIdAndStatusAndPriorityOrderByCreatedAtDesc(Long userId, String status, String priority);

    List<Task> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    List<Task> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, String status);

    List<Task> findByGroupIdAndPriorityOrderByCreatedAtDesc(Long groupId, String priority);

    List<Task> findByGroupIdAndStatusAndPriorityOrderByCreatedAtDesc(Long groupId, String status, String priority);
}
