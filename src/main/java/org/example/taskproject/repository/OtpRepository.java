package org.example.taskproject.repository;


import jakarta.transaction.Transactional;
import org.example.taskproject.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtpRepository extends JpaRepository<Otp,Long> {
    List<Otp> findByEmailOrderByExpiryTimeDesc(String email);

    @Transactional
    void deleteByUsed(boolean used);
}
