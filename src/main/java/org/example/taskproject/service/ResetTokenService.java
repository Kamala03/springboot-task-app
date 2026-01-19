package org.example.taskproject.service;

import java.security.NoSuchAlgorithmException;

public interface ResetTokenService {

    void sendResetPasswordToken(String email);

    boolean checkToken(String email, String token) throws NoSuchAlgorithmException;
}
