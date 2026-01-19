package org.example.taskproject.service;

public interface OtpService {

    void sendOtp(String email);

    boolean checkOtp(String email, String otpCode);
}
