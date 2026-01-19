package org.example.taskproject.repository;


import org.example.taskproject.entity.BlacklistedToken;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.Optional;

public interface BlacklistedTokenRepository extends CrudRepository<BlacklistedToken,Long> {
    Optional<BlacklistedToken> findByJti(String jti);

    void deleteByExpiryDateBefore(Instant now);
}
