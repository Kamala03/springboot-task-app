package org.example.taskproject.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskproject.entity.Otp;
import org.example.taskproject.exception.NotFoundException;
import org.example.taskproject.repository.OtpRepository;
import org.example.taskproject.service.EmailService;
import org.example.taskproject.service.OtpService;
import org.example.taskproject.util.OtpGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpGenerator otpGenerator;
    private final EmailService emailService;
    private final OtpRepository otpRepository;

    @Override
    @Transactional
    public void sendOtp(String email) {
        String otpCode = otpGenerator.generateOtp();

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setCode(otpCode);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(3));
        otp.setUsed(false);

        otpRepository.save(otp);
        log.info("OTP generated and saved for email: {}", email);

        emailService.sendOtpEmail(email, otpCode);
        log.info("OTP email sent to: {}", email);
    }

    @Override
    @Transactional
    public boolean checkOtp(String email, String otpCode) {
        Otp otp = otpRepository.findByEmailOrderByExpiryTimeDesc(email)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No OTP found for email: {}", email);
                    return new NotFoundException("OTP_NOT_FOUND");
                });

        if (otp.isUsed()) {
            log.warn("OTP already used for email: {}", email);
            return false;
        }

        boolean valid = otp.getCode().equals(otpCode) && otp.getExpiryTime().isAfter(LocalDateTime.now());

        if (valid) {
            otp.setUsed(true);
            otpRepository.save(otp);
            log.info("OTP verified successfully for email: {}", email);
        } else {
            log.warn("OTP verification failed for email: {}", email);
        }

        return valid;
    }
}
