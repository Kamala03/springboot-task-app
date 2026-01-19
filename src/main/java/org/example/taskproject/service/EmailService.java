package org.example.taskproject.service;

public interface EmailService {


    void sendOtpEmail(String to, String otp);

    void sendToken(String to, String token);
}
