package org.example.taskproject.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskproject.dto.CardResponse;
import org.example.taskproject.entity.CardEntity;
import org.example.taskproject.entity.UserEntity;
import org.example.taskproject.enums.CardStatus;
import org.example.taskproject.exception.AccessDeniedException;
import org.example.taskproject.exception.CardInactiveException;
import org.example.taskproject.exception.NotFoundException;
import org.example.taskproject.exception.UnsufficientBalanceException;
import org.example.taskproject.mapper.CardMapper;
import org.example.taskproject.repository.CardRepository;
import org.example.taskproject.repository.UserRepository;
import org.example.taskproject.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Transactional(readOnly = true)
    public Page<CardResponse> getCards(Authentication authentication, int page, int size) {
        String email = authentication.getName();
        log.info("Fetching cards for user={}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new NotFoundException("USER_NOT_FOUND");
                });

        Pageable pageable = PageRequest.of(page, size, Sort.by("expirationDate").descending());
        return cardRepository.findByUserId(user.getId(), pageable)
                .map(cardMapper::toCardDtoResponse);
    }


    @Transactional(readOnly = true)
    public BigDecimal getBalance(Authentication authentication, String cardNumber) {
        String email = authentication.getName();
        log.info("Getting balance for card={} user={}", cardNumber, email);

        CardEntity card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException("CARD_NOT_FOUND"));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        if (!card.getUser().getId().equals(user.getId())) {
            log.warn("Access denied to card={} user={}", cardNumber, email);
            throw new AccessDeniedException("CARD_DOES_NOT_BELONG_TO_USER");
        }

        if (card.getCardStatus() != CardStatus.ACTIVE) {
            log.warn("Inactive card access attempt: {}", cardNumber);
            throw new CardInactiveException("CARD_NOT_ACTIVE");
        }

        return card.getBalance();
    }


    @Transactional
    public void transaction(Authentication authentication,
                            String cardNumberFrom,
                            String cardNumberTo,
                            BigDecimal amount) {

        String email = authentication.getName();
        log.info("Transaction request: from={} to={} amount={} user={}",
                cardNumberFrom, cardNumberTo, amount, email);

        CardEntity cardFrom = cardRepository.findByCardNumber(cardNumberFrom)
                .orElseThrow(() -> new NotFoundException("CARD_FROM_NOT_FOUND"));

        CardEntity cardTo = cardRepository.findByCardNumber(cardNumberTo)
                .orElseThrow(() -> new NotFoundException("CARD_TO_NOT_FOUND"));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        if (!cardFrom.getUser().getId().equals(user.getId())
                || !cardTo.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized transfer attempt by user={}", email);
            throw new AccessDeniedException("CARD_DOES_NOT_BELONG_TO_USER");
        }

        if (cardFrom.getCardStatus() != CardStatus.ACTIVE
                || cardTo.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardInactiveException("CARD_NOT_ACTIVE");
        }

        if (cardFrom.getBalance().compareTo(amount) < 0) {
            throw new UnsufficientBalanceException("UNSUFFICIENT_BALANCE");
        }

        cardFrom.setBalance(cardFrom.getBalance().subtract(amount));
        cardTo.setBalance(cardTo.getBalance().add(amount));

        log.info("Transaction successful");
    }

    @Transactional
    public void requestBlockCard(Authentication authentication, String cardNumber) {
        String email = authentication.getName();
        log.info("Block card request: card={} user={}", cardNumber, email);

        CardEntity card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException("CARD_NOT_FOUND"));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("CARD_DOES_NOT_BELONG_TO_USER");
        }

        if (card.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardInactiveException("CARD_NOT_ACTIVE");
        }

        card.setCardStatus(CardStatus.BLOCKED);
        log.info("Card blocked: {}", cardNumber);
    }
}
