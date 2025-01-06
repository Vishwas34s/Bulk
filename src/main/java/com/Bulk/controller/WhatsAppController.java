package com.Bulk.controller;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-sender}")
    private String whatsappSender;

    // Initialize Twilio after the properties are injected
    @PostConstruct
    public void initializeTwilio() {
        Twilio.init(accountSid, authToken);
    }

    // Endpoint to send a message to a single number
    @PostMapping("/send")
    public String sendMessage(@RequestParam String to, @RequestParam String message) {
        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber("whatsapp:" + to),
                    new PhoneNumber(whatsappSender),
                    message
            ).create();

            return "Message sent successfully. SID: " + twilioMessage.getSid();
        } catch (Exception e) {
            return "Failed to send message: " + e.getMessage();
        }
    }

    // Endpoint to send messages in bulk using a CSV file
    @PostMapping("/send-bulk")
    public String sendBulkMessages(@RequestParam("file") MultipartFile file, @RequestParam String message) {
        List<String> failedNumbers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Message.creator(
                            new PhoneNumber("whatsapp:" + line.trim()),
                            new PhoneNumber(whatsappSender),
                            message
                    ).create();
                } catch (Exception e) {
                    failedNumbers.add(line.trim());
                }
            }
        } catch (Exception e) {
            return "Failed to process the file: " + e.getMessage();
        }

        if (failedNumbers.isEmpty()) {
            return "All messages sent successfully!";
        } else {
            return "Failed to send messages to the following numbers: " + String.join(", ", failedNumbers);
        }
    }
}
