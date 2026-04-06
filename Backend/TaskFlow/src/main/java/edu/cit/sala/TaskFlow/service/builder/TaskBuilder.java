package edu.cit.sala.TaskFlow.service.builder;

import edu.cit.sala.TaskFlow.entity.Group;
import edu.cit.sala.TaskFlow.entity.Task;
import edu.cit.sala.TaskFlow.entity.User;

import java.time.LocalDate;

/**
 * Builder Pattern - Separates the construction of a Task from its representation.
 *
 * Before: Task creation logic was inline in TaskService.createTask() with
 * scattered null-checks and default assignments mixed with business logic.
 *
 * After: TaskBuilder encapsulates construction with sensible defaults,
 * fluent API, and a clean build() method. Makes task creation readable
 * and consistent across the codebase.
 */
public class TaskBuilder {

    private String title;
    private String description;
    private String priority = "MEDIUM";
    private String status = "TODO";
    private LocalDate dueDate;
    private User user;
    private Group group;

    public TaskBuilder title(String title) {
        this.title = title;
        return this;
    }

    public TaskBuilder description(String description) {
        this.description = description;
        return this;
    }

    public TaskBuilder priority(String priority) {
        this.priority = (priority != null && !priority.isBlank()) ? priority : "MEDIUM";
        return this;
    }

    public TaskBuilder status(String status) {
        this.status = (status != null && !status.isBlank()) ? status : "TODO";
        return this;
    }

    public TaskBuilder dueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public TaskBuilder user(User user) {
        this.user = user;
        return this;
    }

    public TaskBuilder group(Group group) {
        this.group = group;
        return this;
    }

    public Task build() {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title is required");
        }
        if (user == null) {
            throw new IllegalArgumentException("Task must have an assigned user");
        }

        return Task.builder()
                .title(title)
                .description(description)
                .priority(priority)
                .status(status)
                .dueDate(dueDate)
                .user(user)
                .group(group)
                .build();
    }
}
