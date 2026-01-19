package org.example.taskproject.service;

import org.example.taskproject.dto.UserDtoRequest;
import org.example.taskproject.dto.UserDtoResponse;
import org.springframework.data.domain.Page;

public interface AdminUserService {

    void createAdmin(UserDtoRequest dto);

    void blockUser(Long id);

    void deleteUser(Long id);

    void activateUser(Long id);

    Page<UserDtoResponse> getAllUsers(int page, int size);
}
