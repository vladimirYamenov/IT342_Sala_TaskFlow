package edu.cit.sala.TaskFlow.service.notification;

/**
 * Factory Method Pattern - Concrete product.
 * Welcome email sent to newly registered users.
 */
public class WelcomeNotification implements Notification {

    private final String recipientEmail;
    private final String fullName;

    public WelcomeNotification(String recipientEmail, String fullName) {
        this.recipientEmail = recipientEmail;
        this.fullName = fullName;
    }

    @Override
    public String getSubject() {
        return "Welcome to TaskFlow!";
    }

    @Override
    public String getBody() {
        return "Hi " + fullName + ",\n\n" +
                "Welcome to TaskFlow! Your account has been created successfully.\n\n" +
                "You can now start creating and managing your tasks.\n\n" +
                "Best regards,\n" +
                "The TaskFlow Team";
    }

    @Override
    public String getRecipient() {
        return recipientEmail;
    }
}
