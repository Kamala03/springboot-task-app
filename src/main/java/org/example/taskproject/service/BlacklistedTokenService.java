package org.example.taskproject.service;

import java.time.Instant;

public interface BlacklistedTokenService {


    void blacklist(String jti, Instant expiry);

    boolean isBlacklisted(String jti);

    void cleanup();
}
