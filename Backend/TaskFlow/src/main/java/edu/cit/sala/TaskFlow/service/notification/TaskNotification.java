package edu.cit.sala.TaskFlow.service.notification;

/**
 * Factory Method Pattern - Concrete product.
 * Notification sent when a task event occurs (created, updated, completed).
 */
public class TaskNotification implements Notification {

    private final String recipientEmail;
    private final String taskTitle;
    private final String eventMessage;

    public TaskNotification(String recipientEmail, String taskTitle, String eventMessage) {
        this.recipientEmail = recipientEmail;
        this.taskTitle = taskTitle;
        this.eventMessage = eventMessage;
    }

    @Override
    public String getSubject() {
        return "TaskFlow: " + taskTitle;
    }

    @Override
    public String getBody() {
        return eventMessage;
    }

    @Override
    public String getRecipient() {
        return recipientEmail;
    }
}
