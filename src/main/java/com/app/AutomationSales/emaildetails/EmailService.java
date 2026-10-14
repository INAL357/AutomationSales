package com.app.AutomationSales.emaildetails;

public interface EmailService{

    // Method to send simple email
    void sendSimpleMail(EmailDetails details);

    // Method to send email with attachment
    void sendMailWithAttachment(EmailDetails details);
}
