package org.example.taskproject.service;

import org.example.taskproject.dto.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.security.NoSuchAlgorithmException;

public interface AuthService {

    void signUp(UserDtoRequest dto);

    void signIn(AuthRequest authRequest);

    AuthResponse verifyOtpForSignIn(OtpDto otpDto);

    void resetPassword(HttpServletRequest request, Authentication authentication, ResetPasswordDto passwordDto);

    void forgotPassword(String email);

    void verifyOtpForReset(OtpDto otpDto);

    void verifyResetToken(ForgotPasswordDto forgotPasswordDto, String token) throws NoSuchAlgorithmException;

    void logout(HttpServletRequest request);
}
