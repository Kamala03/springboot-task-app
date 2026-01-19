package org.example.taskproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.taskproject.dto.CardResponse;
import org.example.taskproject.dto.CardRequest;
import org.example.taskproject.service.impl.AdminCardServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Cards", description = "Endpoints for managing cards by admins")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/cards")
public class AdminCardController {

    private final AdminCardServiceImpl adminService;

    @Operation(
            summary = "Create a new card for a user",
            description = "Creates a new card for the user identified by ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Card successfully created")
    @PostMapping("/create-card/{id}")
    public void createCard(
            @Parameter(description = "ID of the user to assign the card to") @PathVariable Long id,
            @RequestBody(description = "Card data including card number and initial balance") @Valid @org.springframework.web.bind.annotation.RequestBody CardRequest cardRequest
    ) {
        adminService.createCard(id, cardRequest);
    }

    @Operation(
            summary = "Block a card",
            description = "Blocks a card by its ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Card successfully blocked")
    @PatchMapping("/block-card/{id}")
    public void blockCard(
            @Parameter(description = "ID of the card to block") @PathVariable Long id
    ) {
        adminService.blockCard(id);
    }

    @Operation(
            summary = "Delete a card",
            description = "Deletes a card by its ID. This can be a soft delete or hard delete depending on implementation."
    )
    @ApiResponse(responseCode = "200", description = "Card successfully deleted")
    @DeleteMapping("/delete-card/{id}")
    public void deleteCard(
            @Parameter(description = "ID of the card to delete") @PathVariable Long id
    ) {
        adminService.deleteCard(id);
    }

    @Operation(
            summary = "Get all cards",
            description = "Returns a paginated list of all cards. Supports `page` and `size` query parameters."
    )
    @ApiResponse(responseCode = "200", description = "List of cards returned")
    @GetMapping("/cards")
    public Page<CardResponse> getCards(
            @Parameter(description = "Page number, zero-based index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of cards per page") @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getAllCards(page, size);
    }
}
