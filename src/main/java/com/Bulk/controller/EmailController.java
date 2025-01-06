package com.Bulk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.Bulk.entity.EmailDetails;
import com.Bulk.entity.EmailSchedule;
import com.Bulk.service.EmailService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;
     
    @PostMapping("/send")
    public ResponseEntity<String> sendBulkEmail(
            @RequestParam("senderEmail") String senderEmail,
            @RequestParam("password") String password,
            @RequestParam(value = "recipientsFile", required = false) MultipartFile recipientsFile, // Optional
            @RequestParam(value = "manualRecipients", required = false) String[] manualRecipients, // Optional
            @RequestParam("subject") String subject,
            @RequestParam("htmlTemplate") MultipartFile htmlTemplate,
            @RequestParam("time") String time, // Time input (e.g., "16:15")
            @RequestParam("days") String days // Days input (e.g., "Monday,Tuesday")
    ) {
        try {
            // Validate inputs
            if (!isValidEmail(senderEmail)) {
                return ResponseEntity.badRequest().body("Error: Invalid sender email.");
            }
            List<String> recipients = new ArrayList<>();
            if (recipientsFile != null && !recipientsFile.isEmpty()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(recipientsFile.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        recipients.add(line.trim());
                    }
                }
            }
            if (manualRecipients != null && manualRecipients.length > 0) {
                recipients.addAll(Arrays.asList(manualRecipients));
            }
            if (recipients.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: No recipients provided. Please upload a file or provide manual recipients.");
            }
            // Validate time format
            try {
                LocalTime.parse(time);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error: Invalid time format. Please provide time in HH:mm format.");
            }
            // Validate days
            List<String> daysList = Arrays.asList(days.split(","));
            if (!areValidDays(daysList)) {
                return ResponseEntity.badRequest().body("Error: Invalid days. Please provide valid weekdays.");
            }
            // Create EmailDetails object
            EmailDetails emailDetails = new EmailDetails(senderEmail, password, recipients, subject, new String(htmlTemplate.getBytes()));
            // Create EmailSchedule object
            EmailSchedule emailSchedule = new EmailSchedule(LocalTime.parse(time), daysList);
            // Schedule email sending
            emailService.scheduleEmailSending(emailDetails, emailSchedule);

            return ResponseEntity.ok("Emails are scheduled to be sent at " + time + " on " + days + ".");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    // Email format validation
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    // Days validation
    private boolean areValidDays(List<String> daysList) {
        List<String> validDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        return daysList.stream().allMatch(validDays::contains);
    }
    @GetMapping("/health")
    public String health() {
        return "API is running!";
    }
}