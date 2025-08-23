package com.hotelbooking.controller;

import com.hotelbooking.entity.ContactMessage;
import com.hotelbooking.service.CaptchaService;
import com.hotelbooking.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // React frontend URL
public class ContactController {

    private final ContactService contactService;
    private final CaptchaService captchaService;

    public ContactController(ContactService contactService, CaptchaService captchaService) {
        this.contactService = contactService;
        this.captchaService = captchaService;
    }

    @PostMapping("/contact")
    public ResponseEntity<String> submitContact(@RequestBody ContactRequest request) {
        // 1️⃣ Verify Captcha
        if (!captchaService.verifyCaptcha(request.getCaptchaToken())) {
            return ResponseEntity.badRequest().body("Captcha verification failed! You might be a bot.");
        }

        // 2️⃣ Save message if captcha is valid
        ContactMessage message = new ContactMessage();
        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setSubject(request.getSubject());
        message.setMessage(request.getMessage());

        contactService.saveMessage(message);

        return ResponseEntity.ok("Message received successfully!");
    }

    // DTO class for request
    public static class ContactRequest {
        private String name;
        private String email;
        private String subject;
        private String message;
        private String captchaToken; // frontend से आएगा

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getCaptchaToken() { return captchaToken; }
        public void setCaptchaToken(String captchaToken) { this.captchaToken = captchaToken; }
    }
}
