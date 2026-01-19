package org.example.taskproject.util;

import lombok.RequiredArgsConstructor;
import org.example.taskproject.service.impl.BlacklistedTokenServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class BlacklistCleanupScheduler {
    private final BlacklistedTokenServiceImpl blacklistService;

    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void cleanupExpiredTokens() {
        System.out.println("Running blacklist cleanup at " + Instant.now());
        blacklistService.cleanup();
    }
}
