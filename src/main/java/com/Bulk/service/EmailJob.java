package com.Bulk.service;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.Bulk.entity.EmailDetails;

import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;

import java.util.List;

@Component // Ensures that EmailJob is managed by Spring
public class EmailJob implements Job {

    private final JavaMailSender mailSender;

    public EmailJob(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            EmailDetails emailDetails = (EmailDetails) context.getJobDetail().getJobDataMap().get("emailDetails");
            List<String> batch = (List<String>) context.getJobDetail().getJobDataMap().get("batch");

            for (String recipient : batch) {
                sendEmail(emailDetails, recipient);
            }
        } catch (Exception e) {
            throw new JobExecutionException("Error sending emails", e);
        }
    }

    private void sendEmail(EmailDetails emailDetails, String recipient) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(emailDetails.getSenderEmail());
        helper.setTo(recipient);
        helper.setSubject(emailDetails.getSubject());
        helper.setText(emailDetails.getHtmlContent(), true);

        mailSender.send(message);
    }
}
