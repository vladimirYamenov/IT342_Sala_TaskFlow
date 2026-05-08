package edu.cit.sala.TaskFlow.feature.task;

import edu.cit.sala.TaskFlow.feature.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT DISTINCT t FROM Task t JOIN t.assignedUsers au WHERE au.id = :userId ORDER BY t.createdAt DESC")
    List<Task> findByAssignedUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(au) > 0 THEN true ELSE false END FROM Task t JOIN t.assignedUsers au WHERE t.id = :taskId AND au.id = :userId")
    boolean isUserAssignedToTask(@Param("taskId") Long taskId, @Param("userId") Long userId);
}
