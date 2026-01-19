package org.example.taskproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.taskproject.dto.*;
import org.example.taskproject.service.impl.AuthServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.NoSuchAlgorithmException;

@Tag(name = "Authentication", description = "Endpoints for user registration, login, password reset, and logout")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl registerService;

    @Operation(
            summary = "Sign up a new user",
            description = "Registers a new user with username, email, and password."
    )
    @ApiResponse(responseCode = "200", description = "User successfully registered")
    @PostMapping("/sign-up")
    public void signUp(
            @RequestBody(description = "User data for registration") @Valid @org.springframework.web.bind.annotation.RequestBody UserDtoRequest userDtoRequest
    ) {
        registerService.signUp(userDtoRequest);
    }

    @Operation(
            summary = "Sign in a user",
            description = "Authenticates user and returns JWT token."
    )
    @ApiResponse(responseCode = "200", description = "User successfully signed in")
    @PostMapping("/sign-in")
    public void signIn(
            @RequestBody(description = "Login credentials") @Valid @org.springframework.web.bind.annotation.RequestBody AuthRequest authRequest
    ) {
        registerService.signIn(authRequest);
    }

    @Operation(
            summary = "Verify OTP for sign in",
            description = "Verifies the OTP code sent to email for completing sign-in."
    )
    @ApiResponse(responseCode = "200", description = "OTP successfully verified")
    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(
            @RequestBody(description = "OTP code and email") @Valid @org.springframework.web.bind.annotation.RequestBody OtpDto otpDto
    ) {
        return registerService.verifyOtpForSignIn(otpDto);
    }

    @Operation(
            summary = "Reset password",
            description = "Allows logged-in users to reset their password."
    )
    @ApiResponse(responseCode = "200", description = "Password successfully reset")
    @PostMapping("/reset-password")
    public void resetPassword(
            HttpServletRequest request,
            Authentication authentication,
            @RequestBody(description = "Old and new password") @Valid @org.springframework.web.bind.annotation.RequestBody ResetPasswordDto passwordDto
    ) {
        registerService.resetPassword(request, authentication, passwordDto);
    }

    @Operation(
            summary = "Request forgot password",
            description = "Sends an OTP to the user's email for password recovery."
    )
    @ApiResponse(responseCode = "200", description = "OTP sent to email")
    @PostMapping("/forgot-password")
    public void forgotPassword(
            @Parameter(description = "Email of the user") @RequestParam String email
    ) {
        registerService.forgotPassword(email);
    }

    @Operation(
            summary = "Verify OTP for password reset",
            description = "Verifies OTP sent for password recovery."
    )
    @ApiResponse(responseCode = "200", description = "OTP verified successfully")
    @PostMapping("/verify-reset-otp")
    public void verifyResetOtp(
            @RequestBody(description = "OTP code and email") @Valid @org.springframework.web.bind.annotation.RequestBody OtpDto otpDto
    ) {
        registerService.verifyOtpForReset(otpDto);
    }

    @Operation(
            summary = "Change password using reset token",
            description = "Changes user's password using token sent via email after forgot password request."
    )
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @PostMapping("/change-password")
    public void changePassword(
            @RequestBody(description = "New password details") @Valid @org.springframework.web.bind.annotation.RequestBody ForgotPasswordDto forgotPasswordDto,
            @Parameter(description = "Token received by email") @RequestParam String token
    ) throws NoSuchAlgorithmException {
        registerService.verifyResetToken(forgotPasswordDto, token);
    }

    @Operation(
            summary = "Logout",
            description = "Logs out the current user by invalidating JWT token."
    )
    @ApiResponse(responseCode = "200", description = "User successfully logged out")
    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        registerService.logout(request);
    }
}
