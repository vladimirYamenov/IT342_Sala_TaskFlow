package edu.cit.sala.TaskFlow.service.strategy;

import edu.cit.sala.TaskFlow.entity.Task;
import edu.cit.sala.TaskFlow.repository.TaskRepository;

import java.util.List;

/**
 * Strategy Pattern - Concrete strategy for filtering tasks by group.
 * Encapsulates the if-else branching logic that was previously
 * inline in TaskService.getGroupTasks().
 */
public class GroupTaskFilterStrategy implements TaskFilterStrategy {

    private final TaskRepository taskRepository;

    public GroupTaskFilterStrategy(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> filter(Long groupId, String status, String priority) {
        if (status != null && priority != null) {
            return taskRepository.findByGroupIdAndStatusAndPriorityOrderByCreatedAtDesc(groupId, status, priority);
        } else if (status != null) {
            return taskRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, status);
        } else if (priority != null) {
            return taskRepository.findByGroupIdAndPriorityOrderByCreatedAtDesc(groupId, priority);
        } else {
            return taskRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        }
    }
}
