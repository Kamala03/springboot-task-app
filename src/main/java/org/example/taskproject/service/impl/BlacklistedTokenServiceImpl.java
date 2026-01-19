package org.example.taskproject.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskproject.entity.BlacklistedToken;
import org.example.taskproject.repository.BlacklistedTokenRepository;
import org.example.taskproject.service.BlacklistedTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistedTokenServiceImpl implements BlacklistedTokenService {

    private final BlacklistedTokenRepository repository;

    @Transactional
    public void blacklist(String jti, Instant expiry) {
        log.info("Blacklisting token: jti={}, expiry={}", jti, expiry);
        repository.save(new BlacklistedToken(jti, expiry));
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        boolean result = repository.findByJti(jti).isPresent();
        log.debug("Token blacklist check: jti={}, blacklisted={}", jti, result);
        return result;
    }

    @Transactional
    public void cleanup() {
        log.info("Cleaning up expired blacklisted tokens");
        repository.deleteByExpiryDateBefore(Instant.now());
        log.info("Expired blacklisted tokens cleanup complete");
    }
}
