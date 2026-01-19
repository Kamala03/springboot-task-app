package org.example.taskproject.repository;

import org.example.taskproject.entity.PasswordResetToken;
import org.example.taskproject.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {

    Optional<PasswordResetToken> findByUserOrderByExpiryDateDesc(UserEntity user);

    void deleteByUsed(boolean used);
}
