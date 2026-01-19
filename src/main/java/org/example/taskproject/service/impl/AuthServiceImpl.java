package org.example.taskproject.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskproject.dto.*;
import org.example.taskproject.entity.RoleEntity;
import org.example.taskproject.entity.UserEntity;
import org.example.taskproject.enums.RoleName;
import org.example.taskproject.enums.UserStatus;
import org.example.taskproject.exception.AlreadyExistException;
import org.example.taskproject.exception.NotFoundException;
import org.example.taskproject.exception.PasswordMismatchException;
import org.example.taskproject.mapper.UserMapper;
import org.example.taskproject.repository.RoleRepository;
import org.example.taskproject.repository.UserRepository;
import org.example.taskproject.service.*;
import org.example.taskproject.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final OtpServiceImpl otpService;
    private final JwtUtil jwtUtil;
    private final ResetTokenServiceImpl resetTokenService;
    private final BlacklistedTokenServiceImpl blacklistedTokenService;

    @Override
    @Transactional
    public void signUp(UserDtoRequest dto) {
        log.info("Signing up user: {}", dto.getEmail());

        UserEntity userEntity = userMapper.toEntity(dto);

        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("Username already exists: {}", dto.getUsername());
            throw new AlreadyExistException("USERNAME_ALREADY_EXIST");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email already exists: {}", dto.getEmail());
            throw new AlreadyExistException("EMAIL_ALREADY_EXIST");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("Password mismatch during sign-up for email: {}", dto.getEmail());
            throw new PasswordMismatchException("PASSWORD_MISMATCH");
        }

        userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        userEntity.setUserStatus(UserStatus.ACTIVE);

        RoleEntity userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> {
                    log.error("ROLE_NOT_FOUND");
                    return new NotFoundException("ROLE_NOT_FOUND");
                });

        userEntity.setRoles(List.of(userRole));
        userRepository.save(userEntity);

        log.info("User signed up successfully: {}", dto.getEmail());
    }

    @Override
    public void signIn(AuthRequest authRequest) {
        log.info("Signing in user: {}", authRequest.getEmail());

        var user = customUserDetailsService.loadUserByUsername(authRequest.getEmail());

        if (user == null || !passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            log.warn("Incorrect credentials for email: {}", authRequest.getEmail());
            throw new BadCredentialsException("Incorrect username or password");
        }

        otpService.sendOtp(authRequest.getEmail());
        log.info("OTP sent to user: {}", authRequest.getEmail());
    }

    @Override
    public AuthResponse verifyOtpForSignIn(OtpDto otpDto) {
        log.info("Verifying OTP for sign-in: {}", otpDto.getEmail());

        if (!otpService.checkOtp(otpDto.getEmail(), otpDto.getCode())) {
            log.warn("OTP verification failed for email: {}", otpDto.getEmail());
            throw new BadCredentialsException("VERIFY_FAILED");
        }

        String token = jwtUtil.generateToken(otpDto.getEmail());
        log.info("OTP verified successfully, token generated for: {}", otpDto.getEmail());
        return new AuthResponse(token);
    }

    @Override
    @Transactional
    public void resetPassword(HttpServletRequest request, Authentication authentication, ResetPasswordDto passwordDto) {
        String email = authentication.getName();
        log.info("Resetting password for user: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        if (!passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            log.warn("Old password mismatch for user: {}", email);
            throw new PasswordMismatchException("OLD_PASSWORD_MISMATCH");
        }

        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmNewPassword())) {
            log.warn("New password mismatch for user: {}", email);
            throw new PasswordMismatchException("PASSWORD_MISMATCH");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);

        logout(request);
        log.info("Password reset successful for user: {}", email);
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Forgot password request for: {}", email);

        var user = customUserDetailsService.loadUserByUsername(email);
        if (user == null) {
            log.warn("User not found for forgot password: {}", email);
            throw new NotFoundException("USER_NOT_FOUND");
        }

        otpService.sendOtp(email);
        log.info("OTP sent for password reset to: {}", email);
    }

    @Override
    public void verifyOtpForReset(OtpDto otpDto) {
        log.info("Verifying OTP for reset: {}", otpDto.getEmail());

        if (!otpService.checkOtp(otpDto.getEmail(), otpDto.getCode())) {
            log.warn("OTP verification failed for reset: {}", otpDto.getEmail());
            throw new BadCredentialsException("VERIFY_FAILED");
        }

        resetTokenService.sendResetPasswordToken(otpDto.getEmail());
        log.info("Reset token sent for user: {}", otpDto.getEmail());
    }

    @Override
    @Transactional
    public void verifyResetToken(ForgotPasswordDto forgotPasswordDto, String token) throws NoSuchAlgorithmException {
        log.info("Verifying reset token for user: {}", forgotPasswordDto.getEmail());

        if (!resetTokenService.checkToken(forgotPasswordDto.getEmail(), token)) {
            log.warn("Invalid reset token for user: {}", forgotPasswordDto.getEmail());
            throw new BadCredentialsException("VERIFY_FAILED");
        }

        UserEntity user = userRepository.findByEmail(forgotPasswordDto.getEmail())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        if (!forgotPasswordDto.getPassword().equals(forgotPasswordDto.getConfirmPassword())) {
            log.warn("Password mismatch during reset for user: {}", forgotPasswordDto.getEmail());
            throw new PasswordMismatchException("PASSWORD_MISMATCH");
        }

        user.setPassword(passwordEncoder.encode(forgotPasswordDto.getPassword()));
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", forgotPasswordDto.getEmail());
    }

    @Override
    public void logout(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        String jti = jwtUtil.getJti(token);
        Instant expiry = jwtUtil.getExpiration(token);
        blacklistedTokenService.blacklist(jti, expiry);

        log.info("User logged out, token blacklisted: jti={}", jti);
    }
}
