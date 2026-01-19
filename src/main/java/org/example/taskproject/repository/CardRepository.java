package org.example.taskproject.repository;


import org.example.taskproject.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CardRepository extends JpaRepository<CardEntity,Long> {
    Page<CardEntity> findByUserId(Long userId, Pageable pageable);

    Optional<CardEntity> findByCardNumber(String cardNumber);
}
