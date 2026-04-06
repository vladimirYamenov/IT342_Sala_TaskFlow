package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.dto.TaskRequest;
import edu.cit.sala.TaskFlow.dto.TaskResponse;
import edu.cit.sala.TaskFlow.entity.Group;
import edu.cit.sala.TaskFlow.entity.Task;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.repository.GroupRepository;
import edu.cit.sala.TaskFlow.repository.GroupMemberRepository;
import edu.cit.sala.TaskFlow.repository.TaskRepository;
import edu.cit.sala.TaskFlow.service.builder.TaskBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * Refactored to use the Builder Pattern via TaskBuilder.
     * Before: inline Task.builder() with scattered null-checks for defaults.
     * After: TaskBuilder handles defaults and validation internally.
     */
    public TaskResponse createTask(TaskRequest request, User user) {
        Group group = null;
        if (request.getGroupId() != null) {
            group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
            if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group");
            }
        }

        try {
            Task task = new TaskBuilder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .priority(request.getPriority())
                    .status(request.getStatus())
                    .dueDate(request.getDueDate())
                    .user(user)
                    .group(group)
                    .build();

            Task saved = taskRepository.save(task);
            return toResponse(saved);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public List<TaskResponse> getUserTasks(Long userId, String status, String priority) {
        List<Task> tasks;

        if (status != null && priority != null) {
            tasks = taskRepository.findByUserIdAndStatusAndPriorityOrderByCreatedAtDesc(userId, status, priority);
        } else if (status != null) {
            tasks = taskRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else if (priority != null) {
            tasks = taskRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(userId, priority);
        } else {
            tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return tasks.stream().map(this::toResponse).toList();
    }

    public TaskResponse getTaskById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return toResponse(task);
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        Task updated = taskRepository.save(task);
        return toResponse(updated);
    }

    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        taskRepository.delete(task);
    }

    public List<TaskResponse> getGroupTasks(Long groupId, Long userId, String status, String priority) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group");
        }

        List<Task> tasks;
        if (status != null && priority != null) {
            tasks = taskRepository.findByGroupIdAndStatusAndPriorityOrderByCreatedAtDesc(groupId, status, priority);
        } else if (status != null) {
            tasks = taskRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, status);
        } else if (priority != null) {
            tasks = taskRepository.findByGroupIdAndPriorityOrderByCreatedAtDesc(groupId, priority);
        } else {
            tasks = taskRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        }

        return tasks.stream().map(this::toResponse).toList();
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .userId(task.getUser().getId())
                .groupId(task.getGroup() != null ? task.getGroup().getId() : null)
                .build();
    }
}
