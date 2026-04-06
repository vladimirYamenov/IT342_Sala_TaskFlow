package edu.cit.sala.TaskFlow.service.notification;

import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern - Concrete creator.
 * Creates WelcomeNotification instances.
 */
@Component
public class WelcomeNotificationFactory extends NotificationFactory {

    @Override
    public Notification createNotification(String recipientEmail, String... params) {
        String fullName = params.length > 0 ? params[0] : "User";
        return new WelcomeNotification(recipientEmail, fullName);
    }
}
