package edu.cit.sala.TaskFlow.service.notification;

/**
 * Factory Method Pattern - Abstract creator.
 * Defines the factory method for creating notifications.
 * Subclasses decide which concrete Notification to instantiate.
 */
public abstract class NotificationFactory {

    public abstract Notification createNotification(String recipientEmail, String... params);
}
