package org.example.taskproject.service;

import org.example.taskproject.dto.CardResponse;
import org.example.taskproject.dto.CardRequest;
import org.springframework.data.domain.Page;

public interface AdminCardService {

    void createCard(Long userId, CardRequest cardRequest);

    void blockCard(Long cardId);

    void deleteCard(Long cardId);

    Page<CardResponse> getAllCards(int page, int size);
}
