package com.hotelbooking.service;

import com.hotelbooking.entity.ContactMessage;
import com.hotelbooking.repository.ContactRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final ContactRepository repository;
    private final JavaMailSender mailSender;

    public ContactService(ContactRepository repository, JavaMailSender mailSender) {
        this.repository = repository;
        this.mailSender = mailSender;
    }

    public ContactMessage saveMessage(ContactMessage message) {
        ContactMessage savedMessage = repository.save(message);
        sendEmailToAdmin(savedMessage);
        return savedMessage;
    }

    private void sendEmailToAdmin(ContactMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo("chetanrathodkgn@gmail.com");
            helper.setSubject("New Contact Message: " + message.getSubject());
            helper.setText(
                "You have received a new message:\n\n" +
                "Name: " + message.getName() + "\n" +
                "Email: " + message.getEmail() + "\n" +
                "Message:\n" + message.getMessage(),
                false
            );

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // You can replace this with proper logging
        }
    }
}
