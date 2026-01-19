package org.example.taskproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;
import org.example.taskproject.dto.CardResponse;
import org.example.taskproject.service.impl.UserServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "User", description = "Endpoints for user card operations and transactions")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;

    @Operation(
            summary = "Get all user cards",
            description = "Returns paginated list of all cards associated with the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "List of user's cards retrieved successfully")
    @GetMapping
    public Page<CardResponse> getCards(
            @Parameter(description = "Authentication object automatically provided by Spring Security") Authentication authentication,
            @Parameter(description = "Page number, default is 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, default is 10") @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getCards(authentication, page, size);
    }

    @Operation(
            summary = "Get card balance",
            description = "Returns the balance of a specific card for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Card balance retrieved successfully")
    @GetMapping("/{cardNumber}/balance")
    public BigDecimal getBalance(
            @Parameter(description = "Authentication object automatically provided by Spring Security") Authentication authentication,
            @Parameter(description = "Card number to check balance") @PathVariable String cardNumber
    ) {
        return userService.getBalance(authentication, cardNumber);
    }

    @Operation(
            summary = "Transfer money between cards",
            description = "Transfers a specified amount from one card to another for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Transfer executed successfully")
    @PostMapping("/transfer")
    public void transfer(
            @Parameter(description = "Authentication object automatically provided by Spring Security") Authentication authentication,
            @Parameter(description = "Source card number") @RequestParam String cardNumberFrom,
            @Parameter(description = "Destination card number") @RequestParam String cardNumberTo,
            @Parameter(description = "Amount to transfer") @RequestParam BigDecimal amount
    ) {
        userService.transaction(authentication, cardNumberFrom, cardNumberTo, amount);
    }

    @Operation(
            summary = "Request block for a card",
            description = "Blocks a card for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Card block request processed successfully")
    @PostMapping("/{cardNumber}/block")
    public void requestBlockCard(
            @Parameter(description = "Authentication object automatically provided by Spring Security") Authentication authentication,
            @Parameter(description = "Card number to block") @PathVariable String cardNumber
    ) {
        userService.requestBlockCard(authentication, cardNumber);
    }
}
