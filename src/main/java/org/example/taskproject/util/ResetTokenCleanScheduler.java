package org.example.taskproject.util;


import lombok.RequiredArgsConstructor;
import org.example.taskproject.repository.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ResetTokenCleanScheduler {
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional
    public void cleanup() {
        passwordResetTokenRepository.deleteByUsed(true);
    }
}
