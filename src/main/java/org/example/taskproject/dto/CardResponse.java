package org.example.taskproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.taskproject.enums.CardStatus;
import java.math.BigDecimal;
import java.time.LocalDate;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private String cardNumber;
    private LocalDate expirationDate;
    private CardStatus cardStatus;
    private BigDecimal balance;
    private UserDtoResponse user;
}
