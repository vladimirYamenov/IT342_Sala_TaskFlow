package edu.cit.sala.TaskFlow.service.strategy;

import edu.cit.sala.TaskFlow.entity.Task;
import edu.cit.sala.TaskFlow.repository.TaskRepository;

import java.util.List;

/**
 * Strategy Pattern - Concrete strategy for filtering tasks by user.
 * Encapsulates the if-else branching logic that was previously
 * inline in TaskService.getUserTasks().
 */
public class UserTaskFilterStrategy implements TaskFilterStrategy {

    private final TaskRepository taskRepository;

    public UserTaskFilterStrategy(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> filter(Long userId, String status, String priority) {
        if (status != null && priority != null) {
            return taskRepository.findByUserIdAndStatusAndPriorityOrderByCreatedAtDesc(userId, status, priority);
        } else if (status != null) {
            return taskRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else if (priority != null) {
            return taskRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(userId, priority);
        } else {
            return taskRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
    }
}
