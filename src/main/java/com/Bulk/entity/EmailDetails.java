package com.Bulk.entity;

import java.util.List;

// Entity class to encapsulate email details
public class EmailDetails {
    private String senderEmail;
    private String password;
    private List<String> recipients;
    private String subject;
    private String htmlContent;

    public EmailDetails(String senderEmail, String password, List<String> recipients, String subject, String htmlContent) {
        this.senderEmail = senderEmail;
        this.password = password;
        this.recipients = recipients;
        this.subject = subject;
        this.htmlContent = htmlContent;
    }

    // Getters and setters
    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
}