package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.service.notification.Notification;
import edu.cit.sala.TaskFlow.service.notification.TaskNotificationFactory;
import edu.cit.sala.TaskFlow.service.notification.WelcomeNotificationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Refactored to use the Factory Method Pattern.
 * Instead of hardcoding email content inline, notification factories
 * create the appropriate Notification objects, and this service
 * focuses solely on sending them.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final WelcomeNotificationFactory welcomeNotificationFactory;
    private final TaskNotificationFactory taskNotificationFactory;

    @Value("${taskflow.mail.from}")
    private String fromAddress;

    @Async
    public void sendNotification(Notification notification) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(notification.getRecipient());
            mail.setSubject(notification.getSubject());
            mail.setText(notification.getBody());
            mailSender.send(mail);
            log.info("Notification sent to {}: {}", notification.getRecipient(), notification.getSubject());
        } catch (Exception e) {
            log.warn("Failed to send notification to {}: {}", notification.getRecipient(), e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        Notification notification = welcomeNotificationFactory.createNotification(toEmail, fullName);
        sendNotification(notification);
    }

    @Async
    public void sendTaskNotification(String toEmail, String taskTitle, String message) {
        Notification notification = taskNotificationFactory.createNotification(toEmail, taskTitle, message);
        sendNotification(notification);
    }
}
