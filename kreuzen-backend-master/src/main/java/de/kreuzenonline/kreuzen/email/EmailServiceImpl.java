package de.kreuzenonline.kreuzen.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ResourceBundle;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final ResourceBundle resourceBundle;
    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.smtp.username}")
    private String sender;

    public EmailServiceImpl(JavaMailSender emailSender, ResourceBundle resourceBundle) {
        this.emailSender = emailSender;
        this.resourceBundle = resourceBundle;
    }

    @Async
    void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Override
    public void sendConfirmEmailMessage(String to, String token, String firstName) {
        this.sendSimpleMessage(to, resourceBundle.getString("confirmation-email-subject"), MessageFormat.format(resourceBundle.getString("confirmation-email-body"), firstName, baseUrl, token));
    }

    @Override
    public void sendPasswordResetMessage(String to, String token, String firstName) {
        this.sendSimpleMessage(to, resourceBundle.getString("password-reset-subject"), MessageFormat.format(resourceBundle.getString("password-reset-body"), firstName, baseUrl, token));

    }

    @Override
    public void sendErrorResolvedMessage(String to, String firstName) {
        this.sendSimpleMessage(to, resourceBundle.getString("error-resolved-subject"), MessageFormat.format(resourceBundle.getString("error-resolved-body"), firstName));
    }

    @Override
    public void sendErrorDeclinedMessage(String to, String firstName) {
        this.sendSimpleMessage(to, resourceBundle.getString("error-declined-subject"), MessageFormat.format(resourceBundle.getString("error-declined-body"), firstName));
    }
}

