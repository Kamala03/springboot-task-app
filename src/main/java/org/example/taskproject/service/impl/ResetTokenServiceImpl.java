package org.example.taskproject.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskproject.entity.PasswordResetToken;
import org.example.taskproject.entity.UserEntity;
import org.example.taskproject.exception.NotFoundException;
import org.example.taskproject.repository.PasswordResetTokenRepository;
import org.example.taskproject.repository.UserRepository;
import org.example.taskproject.service.EmailService;
import org.example.taskproject.service.ResetTokenService;
import org.example.taskproject.util.ResetTokenGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetTokenServiceImpl implements ResetTokenService {

    private final ResetTokenGenerator resetTokenGenerator;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendResetPasswordToken(String email) {
        log.info("Generating reset token for email: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        String resetToken = resetTokenGenerator.resetToken();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        passwordResetToken.setUsed(false);

        passwordResetTokenRepository.save(passwordResetToken);
        log.info("Reset token saved for userId={}", user.getId());

        emailService.sendToken(email, resetToken);
        log.info("Reset token email sent to: {}", email);
    }

    @Override
    @Transactional
    public boolean checkToken(String email, String token) throws NoSuchAlgorithmException {
        log.info("Checking reset token for email: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByUserOrderByExpiryDateDesc(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No reset token found for userId={}", user.getId());
                    return new NotFoundException("TOKEN_NOT_FOUND");
                });

        if (token == null || passwordResetToken.isUsed()) {
            log.warn("Invalid or already used token for userId={}", user.getId());
            return false;
        }

        boolean valid = passwordResetToken.getToken().equals(token) &&
                passwordResetToken.getExpiryDate().isAfter(LocalDateTime.now());

        if (valid) {
            passwordResetToken.setUsed(true);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            String hashedToken = Base64.getEncoder().encodeToString(hash);

            passwordResetTokenRepository.save(passwordResetToken);
            log.info("Reset token verified successfully for userId={}", user.getId());
        } else {
            log.warn("Reset token invalid or expired for userId={}", user.getId());
        }

        return valid;
    }
}
