package edu.cit.sala.TaskFlow.service.notification;

/**
 * Factory Method Pattern - Abstract product.
 * Represents a notification with subject and body content.
 */
public interface Notification {
    String getSubject();
    String getBody();
    String getRecipient();
}
