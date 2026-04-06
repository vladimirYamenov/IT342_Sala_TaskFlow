package edu.cit.sala.TaskFlow.service.notification;

import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern - Concrete creator.
 * Creates TaskNotification instances.
 */
@Component
public class TaskNotificationFactory extends NotificationFactory {

    @Override
    public Notification createNotification(String recipientEmail, String... params) {
        String taskTitle = params.length > 0 ? params[0] : "Task Update";
        String message = params.length > 1 ? params[1] : "You have a task update.";
        return new TaskNotification(recipientEmail, taskTitle, message);
    }
}
