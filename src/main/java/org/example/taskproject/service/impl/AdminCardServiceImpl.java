package org.example.taskproject.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskproject.dto.CardResponse;
import org.example.taskproject.dto.CardRequest;
import org.example.taskproject.entity.CardEntity;
import org.example.taskproject.entity.UserEntity;
import org.example.taskproject.enums.CardStatus;
import org.example.taskproject.exception.CardAlreadyBlockedException;
import org.example.taskproject.exception.CardAlreadyDeletedException;
import org.example.taskproject.exception.NotFoundException;
import org.example.taskproject.mapper.CardMapper;
import org.example.taskproject.repository.CardRepository;
import org.example.taskproject.repository.UserRepository;
import org.example.taskproject.service.AdminCardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminCardServiceImpl implements AdminCardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;

    @Override
    public void createCard(Long userId, CardRequest cardRequest) {
        log.info("Creating card for userId={}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: userId={}", userId);
                    return new NotFoundException("USER_NOT_FOUND");
                });

        CardEntity card = cardMapper.toCardEntity(cardRequest);
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setCardStatus(CardStatus.ACTIVE);
        card.setUser(user);

        cardRepository.save(card);
        log.info("Card created successfully for userId={}", userId);
    }

    @Override
    public void blockCard(Long cardId) {
        log.info("Blocking card: cardId={}", cardId);

        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Card not found: cardId={}", cardId);
                    return new NotFoundException("CARD_NOT_FOUND");
                });

        if (card.getCardStatus() == CardStatus.DELETED) {
            log.warn("Attempt to block deleted card: cardId={}", cardId);
            throw new CardAlreadyDeletedException("CARD_ALREADY_DELETED");
        }

        if (card.getCardStatus() == CardStatus.BLOCKED) {
            log.warn("Card already blocked: cardId={}", cardId);
            throw new CardAlreadyBlockedException("CARD_ALREADY_BLOCKED");
        }

        card.setCardStatus(CardStatus.BLOCKED);
        log.info("Card blocked successfully: cardId={}", cardId);
    }

    @Override
    public void deleteCard(Long cardId) {
        log.info("Deleting card: cardId={}", cardId);

        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Card not found: cardId={}", cardId);
                    return new NotFoundException("CARD_NOT_FOUND");
                });

        if (card.getCardStatus() == CardStatus.DELETED) {
            log.warn("Card already deleted: cardId={}", cardId);
            throw new CardAlreadyDeletedException("CARD_ALREADY_DELETED");
        }

        card.setCardStatus(CardStatus.DELETED);
        log.info("Card deleted successfully: cardId={}", cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCards(int page, int size) {
        log.info("Fetching all cards: page={}, size={}", page, size);

        Page<CardResponse> result = cardRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        ).map(cardMapper::toCardDtoResponse);

        log.info("Fetched {} cards", result.getTotalElements());
        return result;
    }
}
