package org.example.taskproject.util;


import lombok.RequiredArgsConstructor;
import org.example.taskproject.repository.OtpRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OtpCleanScheduler {
    private final OtpRepository  otpRepository;


    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional
    public void cleanup() {
        otpRepository.deleteByUsed(true);
    }
}
