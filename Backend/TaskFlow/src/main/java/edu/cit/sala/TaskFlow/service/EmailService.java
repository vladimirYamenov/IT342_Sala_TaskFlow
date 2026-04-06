package edu.cit.sala.TaskFlow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${taskflow.mail.from}")
    private String fromAddress;

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Welcome to TaskFlow!");
            message.setText(
                    "Hi " + fullName + ",\n\n" +
                    "Welcome to TaskFlow! Your account has been created successfully.\n\n" +
                    "You can now start creating and managing your tasks.\n\n" +
                    "Best regards,\n" +
                    "The TaskFlow Team"
            );
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendTaskNotification(String toEmail, String taskTitle, String message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(toEmail);
            mail.setSubject("TaskFlow: " + taskTitle);
            mail.setText(message);
            mailSender.send(mail);
            log.info("Task notification sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send task notification to {}: {}", toEmail, e.getMessage());
        }
    }
}
