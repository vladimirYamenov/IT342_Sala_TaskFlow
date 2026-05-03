package edu.cit.sala.TaskFlow.service.observer;

import edu.cit.sala.TaskFlow.entity.Task;
import org.springframework.context.ApplicationEvent;

/**
 * Observer Pattern - Event object.
 * Represents a task lifecycle event (CREATED, UPDATED, COMPLETED, DELETED).
 * Published by TaskService and consumed by registered listeners.
 */
public class TaskEvent extends ApplicationEvent {

    public enum Type { CREATED, UPDATED, COMPLETED, DELETED }

    private final Task task;
    private final Type type;

    public TaskEvent(Object source, Task task, Type type) {
        super(source);
        this.task = task;
        this.type = type;
    }

    public Task getTask() {
        return task;
    }

    public Type getType() {
        return type;
    }
}
