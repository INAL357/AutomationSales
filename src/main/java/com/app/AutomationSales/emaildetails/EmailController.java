package com.app.AutomationSales.emaildetails;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(
            EmailService emailService) {

        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMail(
            @RequestBody EmailDetails details) {

        emailService.sendSimpleMail(details);

        return ResponseEntity.ok(
                "Mail Sent Successfully"
        );
    }

    @PostMapping("/send-with-attachment")
    public ResponseEntity<String>
    sendMailWithAttachment(
            @RequestBody EmailDetails details) {

        emailService.sendMailWithAttachment(details);

        return ResponseEntity.ok(
                "Mail Sent Successfully"
        );
    }
}