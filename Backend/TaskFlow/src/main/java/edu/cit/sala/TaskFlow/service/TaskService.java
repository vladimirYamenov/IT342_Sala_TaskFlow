package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.dto.TaskRequest;
import edu.cit.sala.TaskFlow.dto.TaskResponse;
import edu.cit.sala.TaskFlow.entity.Group;
import edu.cit.sala.TaskFlow.entity.Task;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.repository.GroupMemberRepository;
import edu.cit.sala.TaskFlow.repository.GroupRepository;
import edu.cit.sala.TaskFlow.repository.TaskRepository;
import edu.cit.sala.TaskFlow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(TaskRequest request, User user) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task title is required");
        }

        Group group = null;
        if (request.getGroupId() != null) {
            group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
            if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group");
            }
        }

        List<User> assignedUsers = resolveAssignedUsers(request.getAssignedUserIds(), group);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .status(request.getStatus() != null ? request.getStatus() : "TODO")
                .dueDate(request.getDueDate())
                .user(user)
                .group(group)
                .assignedUsers(assignedUsers)
                .build();

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public List<TaskResponse> getUserTasks(Long userId, String status, String priority) {
        // Tasks created by user
        List<Task> ownedTasks;
        if (status != null && priority != null) {
            ownedTasks = taskRepository.findByUserIdAndStatusAndPriorityOrderByCreatedAtDesc(userId, status, priority);
        } else if (status != null) {
            ownedTasks = taskRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else if (priority != null) {
            ownedTasks = taskRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(userId, priority);
        } else {
            ownedTasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        // Tasks assigned to user (created by others)
        List<Task> assignedTasks = taskRepository.findByAssignedUserId(userId).stream()
                .filter(t -> !t.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        // Apply filters to assigned tasks in memory
        if (status != null) {
            assignedTasks = assignedTasks.stream()
                    .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                    .collect(Collectors.toList());
        }
        if (priority != null) {
            assignedTasks = assignedTasks.stream()
                    .filter(t -> priority.equalsIgnoreCase(t.getPriority()))
                    .collect(Collectors.toList());
        }

        // Merge using LinkedHashMap to preserve order and de-duplicate by id
        Map<Long, Task> merged = new LinkedHashMap<>();
        for (Task t : ownedTasks) merged.put(t.getId(), t);
        for (Task t : assignedTasks) merged.putIfAbsent(t.getId(), t);

        return merged.values().stream().map(this::toResponse).toList();
    }

    public TaskResponse getTaskById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!hasAccess(task, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return toResponse(task);
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!hasAccess(task, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        boolean isCreator = task.getUser().getId().equals(userId);

        // Only the creator can change structural fields and assignments
        if (isCreator) {
            if (request.getTitle() != null) task.setTitle(request.getTitle());
            if (request.getDescription() != null) task.setDescription(request.getDescription());
            if (request.getPriority() != null) task.setPriority(request.getPriority());
            if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
            if (request.getAssignedUserIds() != null) {
                task.setAssignedUsers(resolveAssignedUsers(request.getAssignedUserIds(), task.getGroup()));
            }
        }

        // Any member with access (creator or assigned) can update status
        if (request.getStatus() != null) task.setStatus(request.getStatus());

        Task updated = taskRepository.save(task);
        return toResponse(updated);
    }

    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        // Allow group members (not just the creator) to delete group tasks
        boolean canDelete = task.getUser().getId().equals(userId)
                || (task.getGroup() != null && groupMemberRepository.existsByGroupIdAndUserId(task.getGroup().getId(), userId));

        if (!canDelete) {
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

    // --- Helpers ---

    private boolean hasAccess(Task task, Long userId) {
        if (task.getUser().getId().equals(userId)) return true;
        if (task.getAssignedUsers().stream().anyMatch(u -> u.getId().equals(userId))) return true;
        // Any member of the group the task belongs to also has access
        if (task.getGroup() != null) {
            return groupMemberRepository.existsByGroupIdAndUserId(task.getGroup().getId(), userId);
        }
        return false;
    }

    private List<User> resolveAssignedUsers(List<Long> ids, Group group) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();

        List<User> users = new ArrayList<>();
        for (Long id : ids) {
            User u = userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Assigned user not found: " + id));
            // If this is a group task, the assignee must be a group member
            if (group != null && !groupMemberRepository.existsByGroupIdAndUserId(group.getId(), id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User " + u.getEmail() + " is not a member of this group");
            }
            users.add(u);
        }
        return users;
    }

    private TaskResponse toResponse(Task task) {
        List<TaskResponse.AssignedUserInfo> assignedUserInfos = task.getAssignedUsers().stream()
                .map(u -> TaskResponse.AssignedUserInfo.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .build())
                .toList();

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
                .assignedUsers(assignedUserInfos)
                .build();
    }
}
