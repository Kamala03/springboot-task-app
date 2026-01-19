package org.example.taskproject;

import org.example.taskproject.dto.CardRequest;
import org.example.taskproject.dto.CardResponse;
import org.example.taskproject.entity.CardEntity;
import org.example.taskproject.entity.UserEntity;
import org.example.taskproject.enums.CardStatus;
import org.example.taskproject.exception.CardAlreadyBlockedException;
import org.example.taskproject.exception.CardAlreadyDeletedException;
import org.example.taskproject.exception.NotFoundException;
import org.example.taskproject.mapper.CardMapper;
import org.example.taskproject.repository.CardRepository;
import org.example.taskproject.repository.UserRepository;
import org.example.taskproject.service.impl.AdminCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminCardServiceImplTest {

    @InjectMocks
    private AdminCardServiceImpl adminCardService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCard_shouldCreateCardSuccessfully() {
        Long userId = 1L;
        CardRequest request = new CardRequest();
        UserEntity user = new UserEntity();
        CardEntity cardEntity = new CardEntity();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardMapper.toCardEntity(request)).thenReturn(cardEntity);
        when(cardRepository.save(cardEntity)).thenReturn(cardEntity);

        adminCardService.createCard(userId, request);

        assertEquals(CardStatus.ACTIVE, cardEntity.getCardStatus());
        assertEquals(user, cardEntity.getUser());
        assertEquals(LocalDate.now().plusYears(2), cardEntity.getExpirationDate());
        verify(cardRepository).save(cardEntity);
    }

    @Test
    void createCard_shouldThrowNotFoundException_whenUserNotFound() {
        Long userId = 1L;
        CardRequest request = new CardRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            adminCardService.createCard(userId, request)
        );

        assertEquals("USER_NOT_FOUND", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCard_shouldBlockCardSuccessfully() {
        Long cardId = 1L;
        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        adminCardService.blockCard(cardId);

        assertEquals(CardStatus.BLOCKED, card.getCardStatus());
    }

    @Test
    void blockCard_shouldThrowCardAlreadyBlockedException() {
        Long cardId = 1L;
        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(CardAlreadyBlockedException.class, () -> adminCardService.blockCard(cardId));
    }

    @Test
    void blockCard_shouldThrowCardAlreadyDeletedException() {
        Long cardId = 1L;
        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.DELETED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(CardAlreadyDeletedException.class, () -> adminCardService.blockCard(cardId));
    }

    @Test
    void deleteCard_shouldDeleteCardSuccessfully() {
        Long cardId = 1L;
        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        adminCardService.deleteCard(cardId);

        assertEquals(CardStatus.DELETED, card.getCardStatus());
    }

    @Test
    void deleteCard_shouldThrowCardAlreadyDeletedException() {
        Long cardId = 1L;
        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.DELETED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(CardAlreadyDeletedException.class, () -> adminCardService.deleteCard(cardId));
    }

    @Test
    void getAllCards_shouldReturnPagedCards() {
        CardEntity card1 = new CardEntity();
        CardEntity card2 = new CardEntity();
        CardResponse response1 = new CardResponse();
        CardResponse response2 = new CardResponse();

        List<CardEntity> cards = List.of(card1, card2);
        Page<CardEntity> page = new PageImpl<>(cards);

        when(cardRepository.findAll(PageRequest.of(0, 2, Sort.by("id").descending()))).thenReturn(page);
        when(cardMapper.toCardDtoResponse(card1)).thenReturn(response1);
        when(cardMapper.toCardDtoResponse(card2)).thenReturn(response2);

        Page<CardResponse> result = adminCardService.getAllCards(0, 2);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(response1));
        assertTrue(result.getContent().contains(response2));
    }
}
