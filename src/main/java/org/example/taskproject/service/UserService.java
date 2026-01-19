package org.example.taskproject.service;

import org.example.taskproject.dto.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

public interface UserService {

    Page<CardResponse> getCards(Authentication authentication, int page, int size);

    BigDecimal getBalance(Authentication authentication, String cardNumber);

    void transaction(Authentication authentication,
                     String cardNumberFrom,
                     String cardNumberTo,
                     BigDecimal amount);

    void requestBlockCard(Authentication authentication, String cardNumber);
}
