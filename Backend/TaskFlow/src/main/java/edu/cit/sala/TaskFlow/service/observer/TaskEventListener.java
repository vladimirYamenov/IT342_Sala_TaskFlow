package edu.cit.sala.TaskFlow.service.observer;

import edu.cit.sala.TaskFlow.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern - Concrete observer.
 * Listens for TaskEvent and sends email notifications when tasks
 * are created or completed.
 *
 * Before: No notification was sent on task lifecycle events.
 * After: This listener automatically reacts to task events,
 * decoupling the notification concern from TaskService.
 * New observers can be added (e.g., logging, analytics) without
 * modifying TaskService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleTaskEvent(TaskEvent event) {
        String userEmail = event.getTask().getUser().getEmail();
        String taskTitle = event.getTask().getTitle();

        switch (event.getType()) {
            case CREATED -> {
                log.info("Task created: '{}' by {}", taskTitle, userEmail);
                emailService.sendTaskNotification(
                        userEmail,
                        taskTitle,
                        "Your task '" + taskTitle + "' has been created successfully."
                );
            }
            case COMPLETED -> {
                log.info("Task completed: '{}' by {}", taskTitle, userEmail);
                emailService.sendTaskNotification(
                        userEmail,
                        taskTitle,
                        "Congratulations! Your task '" + taskTitle + "' has been marked as completed."
                );
            }
            case UPDATED -> log.info("Task updated: '{}' by {}", taskTitle, userEmail);
            case DELETED -> log.info("Task deleted: '{}' by {}", taskTitle, userEmail);
        }
    }
}
