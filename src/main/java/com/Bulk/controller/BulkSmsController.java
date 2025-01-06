package com.Bulk.controller;

import com.Bulk.config.TwilioConfig;
import com.Bulk.entity.SmsRequest;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api/sms")
public class BulkSmsController {

    @Autowired
    private TwilioConfig twilioConfig;

    // Store OTPs temporarily 
    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, Long> otpExpiration = new HashMap<>();

    private static final long OTP_EXPIRATION_TIME = 5 * 60 * 1000; // 5 minutes

    // Send OTP to verify phone number
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody String phoneNumber) {
        String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
        if (formattedPhoneNumber == null) {
            return ResponseEntity.status(400).body("Invalid phone number format.");
        }

        String otp = generateOtp();
        otpStorage.put(formattedPhoneNumber, otp);
        otpExpiration.put(formattedPhoneNumber, System.currentTimeMillis() + OTP_EXPIRATION_TIME);

        // Send OTP to the phone number
        sendMessage(formattedPhoneNumber, "Your OTP is: " + otp);
        return ResponseEntity.ok("OTP sent to the phone number!");
    }

    
 // Verify OTP and proceed with message sending
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestParam String phoneNumber, @RequestParam String otp) {
        String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
        if (formattedPhoneNumber == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid phone number format.");
            return ResponseEntity.status(400).body(response);
        }

        String storedOtp = otpStorage.get(formattedPhoneNumber);
        Long expirationTime = otpExpiration.get(formattedPhoneNumber);

        if (storedOtp == null || expirationTime == null || System.currentTimeMillis() > expirationTime) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP has expired or was not sent.");
            return ResponseEntity.status(400).body(response);
        }

        if (storedOtp.equals(otp)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP verified successfully! You can now send the message.");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid OTP.");
            return ResponseEntity.status(400).body(response);
        }
    }


    
 // Send SMS to manually entered numbers with manual or file-based message
    @PostMapping("/send")
    public ResponseEntity<String> sendSms(
            @RequestBody SmsRequest smsRequest,
            @RequestParam(value = "messageFile", required = false) MultipartFile messageFile) throws IOException {

        String message = smsRequest.getMessage();

        // Validate that at least one message source is provided
        if ((message == null || message.isEmpty()) && (messageFile == null || messageFile.isEmpty())) {
            return ResponseEntity.status(400).body("Either a message or a message file must be provided.");
        }

        // Read message from file if provided
        if (messageFile != null && !messageFile.isEmpty()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(messageFile.getInputStream()))) {
                message = reader.lines().reduce("", (acc, line) -> acc + line + "\n").trim();
            }
        }

        // Send the message to each phone number
        for (String number : smsRequest.getPhoneNumbers()) {
            String formattedNumber = formatPhoneNumber(number);
            if (formattedNumber != null) {
                sendMessage(formattedNumber, message);
            } else {
                return ResponseEntity.status(400).body("Invalid phone number format for: " + number);
            }
        }

        return ResponseEntity.ok("Messages sent successfully!");
    }


    
 // Upload CSV and send SMS with manual or file-based message
    @PostMapping("/send-csv")
    public ResponseEntity<String> sendSmsFromCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "messageFile", required = false) MultipartFile messageFile) throws IOException, CsvException, CsvValidationException {

        // Validate that at least one message source is provided
        if ((message == null || message.isEmpty()) && (messageFile == null || messageFile.isEmpty())) {
            return ResponseEntity.status(400).body("Either a message or a message file must be provided.");
        }

        // Read message from file if provided
        if (messageFile != null && !messageFile.isEmpty()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(messageFile.getInputStream()))) {
                message = reader.lines().reduce("", (acc, line) -> acc + line + "\n").trim();
            }
        }

        // Extract phone numbers from CSV
        List<String> phoneNumbers = extractPhoneNumbersFromCsv(file);
        for (String number : phoneNumbers) {
            String formattedNumber = formatPhoneNumber(number);
            if (formattedNumber != null) {
                sendMessage(formattedNumber, message);
            } else {
                return ResponseEntity.status(400).body("Invalid phone number format in CSV.");
            }
        }
        return ResponseEntity.ok("Messages sent successfully from CSV!");
    }


    // Helper method to send SMS using Twilio API
    private void sendMessage(String to, String message) {
        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    message
            ).create();
        } catch (Exception e) {
            // Log the error
            System.err.println("Error sending message to " + to + ": " + e.getMessage());
        }
    }

    // Helper method to extract phone numbers from the uploaded CSV file
    private List<String> extractPhoneNumbersFromCsv(MultipartFile file) throws IOException, CsvException, CsvValidationException {
        List<String> phoneNumbers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                phoneNumbers.add(line[0]);
            }
        }
        return phoneNumbers;
    }

    // Helper method to generate a random OTP
    private String generateOtp() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(999999));
    }

    // Helper method to format phone numbers to international format (+91XXXXXXXXXX)
    private String formatPhoneNumber(String phoneNumber) {
        // Strip any non-digit characters and check if it starts with the country code
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (phoneNumber.length() == 10) {
            return "+91" + phoneNumber; // Assuming India as country, change the code for other countries
        }
        return null; // Invalid phone number format
    }
}
