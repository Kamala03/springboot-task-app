package org.example.taskproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.taskproject.dto.UserDtoRequest;
import org.example.taskproject.dto.UserDtoResponse;
import org.example.taskproject.service.impl.AdminUserServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Users", description = "Endpoints for managing users by admins")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserServiceImpl adminUserService;

    @Operation(
            summary = "Create a new admin user",
            description = "Creates a new admin user. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Admin user successfully created")
    @PostMapping("/create")
    public void createAdmin(
            @RequestBody(description = "User data for creating an admin") @Valid @org.springframework.web.bind.annotation.RequestBody UserDtoRequest dto
    ) {
        adminUserService.createAdmin(dto);
    }

    @Operation(
            summary = "Block a user",
            description = "Blocks a user account by user ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User successfully blocked")
    @PutMapping("/{id}/block")
    public void blockUser(
            @Parameter(description = "ID of the user to block") @PathVariable Long id
    ) {
        adminUserService.blockUser(id);
    }

    @Operation(
            summary = "Activate a user",
            description = "Activates a blocked user account by user ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User successfully activated")
    @PutMapping("/{id}/activate")
    public void activateUser(
            @Parameter(description = "ID of the user to activate") @PathVariable Long id
    ) {
        adminUserService.activateUser(id);
    }

    @Operation(
            summary = "Delete a user",
            description = "Deletes a user account by user ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User successfully deleted")
    @DeleteMapping("/{id}")
    public void deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable Long id
    ) {
        adminUserService.deleteUser(id);
    }

    @Operation(
            summary = "Get all users",
            description = "Returns a paginated list of all users. Supports `page` and `size` query parameters."
    )
    @ApiResponse(responseCode = "200", description = "List of users returned")
    @GetMapping
    public Page<UserDtoResponse> getAllUsers(
            @Parameter(description = "Page number, zero-based index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of users per page") @RequestParam(defaultValue = "10") int size
    ) {
        return adminUserService.getAllUsers(page, size);
    }
}
