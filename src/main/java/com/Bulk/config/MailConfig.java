package com.Bulk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.util.List;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

     // Use environment variables for authentication
        String email = System.getenv("MAIL_USERNAME");
        String password = System.getenv("MAIL_PASSWORD");
        mailSender.setUsername(email);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    // Method to send email with attachments to multiple recipients
    public void sendBulkEmailWithAttachments(String senderEmail, String senderPassword, List<String> toEmails, String subject, String text, List<File> attachments) throws MessagingException {
        JavaMailSender mailSender = getJavaMailSender();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Authenticate the sender using provided credentials
        JavaMailSenderImpl authenticatedSender = (JavaMailSenderImpl) mailSender;
        authenticatedSender.setUsername(senderEmail);
        authenticatedSender.setPassword(senderPassword);

        // Set the "From" email and subject
        helper.setFrom(senderEmail);
        helper.setSubject(subject);
        helper.setText(text, true);

        // Add recipients as BCC to protect their privacy
        helper.setBcc(toEmails.toArray(new String[0]));

        // Add attachments if provided
        if (attachments != null) {
            for (File file : attachments) {
                helper.addAttachment(file.getName(), file);
            }
        }

        // Send the email
        authenticatedSender.send(message);
    }
}
