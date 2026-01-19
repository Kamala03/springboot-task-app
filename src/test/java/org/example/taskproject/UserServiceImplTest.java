package org.example.taskproject;

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
import org.example.taskproject.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getCards_shouldReturnPagedCards() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity card1 = new CardEntity();
        CardEntity card2 = new CardEntity();
        CardResponse dto1 = new CardResponse();
        CardResponse dto2 = new CardResponse();

        Page<CardEntity> page = new PageImpl<>(List.of(card1, card2));

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(cardMapper.toCardDtoResponse(card1)).thenReturn(dto1);
        when(cardMapper.toCardDtoResponse(card2)).thenReturn(dto2);

        Page<CardResponse> result = userService.getCards(authentication, 0, 2);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(dto1));
        assertTrue(result.getContent().contains(dto2));
    }

    @Test
    void getCards_shouldThrowNotFound_whenUserNotFound() {
        when(authentication.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getCards(authentication, 0, 1));
    }


    @Test
    void getBalance_shouldReturnBalance() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity card = new CardEntity();
        card.setUser(user);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setCardStatus(CardStatus.ACTIVE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1234")).thenReturn(Optional.of(card));

        BigDecimal balance = userService.getBalance(authentication, "1234");
        assertEquals(BigDecimal.valueOf(1000), balance);
    }

    @Test
    void getBalance_shouldThrowAccessDenied_ifCardNotOwned() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        UserEntity otherUser = new UserEntity();
        otherUser.setId(2L);

        CardEntity card = new CardEntity();
        card.setUser(otherUser);
        card.setCardStatus(CardStatus.ACTIVE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1234")).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> userService.getBalance(authentication, "1234"));
    }

    @Test
    void getBalance_shouldThrowCardInactive_ifCardNotActive() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity card = new CardEntity();
        card.setUser(user);
        card.setCardStatus(CardStatus.BLOCKED);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1234")).thenReturn(Optional.of(card));

        assertThrows(CardInactiveException.class, () -> userService.getBalance(authentication, "1234"));
    }


    @Test
    void transaction_shouldTransferAmount() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity from = new CardEntity();
        from.setUser(user);
        from.setBalance(BigDecimal.valueOf(1000));
        from.setCardStatus(CardStatus.ACTIVE);

        CardEntity to = new CardEntity();
        to.setUser(user);
        to.setBalance(BigDecimal.valueOf(500));
        to.setCardStatus(CardStatus.ACTIVE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1111")).thenReturn(Optional.of(from));
        when(cardRepository.findByCardNumber("2222")).thenReturn(Optional.of(to));

        userService.transaction(authentication, "1111", "2222", BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(800), from.getBalance());
        assertEquals(BigDecimal.valueOf(700), to.getBalance());
    }

    @Test
    void transaction_shouldThrowInsufficientBalance() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity from = new CardEntity();
        from.setUser(user);
        from.setBalance(BigDecimal.valueOf(100));
        from.setCardStatus(CardStatus.ACTIVE);

        CardEntity to = new CardEntity();
        to.setUser(user);
        to.setBalance(BigDecimal.valueOf(500));
        to.setCardStatus(CardStatus.ACTIVE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1111")).thenReturn(Optional.of(from));
        when(cardRepository.findByCardNumber("2222")).thenReturn(Optional.of(to));

        assertThrows(UnsufficientBalanceException.class,
                () -> userService.transaction(authentication, "1111", "2222", BigDecimal.valueOf(200)));
    }

    @Test
    void transaction_shouldThrowCardInactive_ifAnyCardInactive() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity from = new CardEntity();
        from.setUser(user);
        from.setBalance(BigDecimal.valueOf(500));
        from.setCardStatus(CardStatus.BLOCKED);

        CardEntity to = new CardEntity();
        to.setUser(user);
        to.setBalance(BigDecimal.valueOf(500));
        to.setCardStatus(CardStatus.ACTIVE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1111")).thenReturn(Optional.of(from));
        when(cardRepository.findByCardNumber("2222")).thenReturn(Optional.of(to));

        assertThrows(CardInactiveException.class,
                () -> userService.transaction(authentication, "1111", "2222", BigDecimal.valueOf(100)));
    }


    @Test
    void requestBlockCard_shouldBlockCard() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity card = new CardEntity();
        card.setUser(user);
        card.setCardStatus(CardStatus.ACTIVE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1234")).thenReturn(Optional.of(card));

        userService.requestBlockCard(authentication, "1234");

        assertEquals(CardStatus.BLOCKED, card.getCardStatus());
    }

    @Test
    void requestBlockCard_shouldThrowAccessDenied_ifCardNotOwned() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        UserEntity otherUser = new UserEntity();
        otherUser.setId(2L);

        CardEntity card = new CardEntity();
        card.setUser(otherUser);
        card.setCardStatus(CardStatus.ACTIVE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1234")).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class,
                () -> userService.requestBlockCard(authentication, "1234"));
    }

    @Test
    void requestBlockCard_shouldThrowCardInactive_ifCardNotActive() {
        when(authentication.getName()).thenReturn("user@test.com");

        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity card = new CardEntity();
        card.setUser(user);
        card.setCardStatus(CardStatus.BLOCKED);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("1234")).thenReturn(Optional.of(card));

        assertThrows(CardInactiveException.class,
                () -> userService.requestBlockCard(authentication, "1234"));
    }
}
