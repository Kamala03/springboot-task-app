package org.example.taskproject.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskproject.dto.UserDtoRequest;
import org.example.taskproject.dto.UserDtoResponse;
import org.example.taskproject.entity.RoleEntity;
import org.example.taskproject.entity.UserEntity;
import org.example.taskproject.enums.RoleName;
import org.example.taskproject.enums.UserStatus;
import org.example.taskproject.exception.AlreadyExistException;
import org.example.taskproject.exception.NotFoundException;
import org.example.taskproject.exception.PasswordMismatchException;
import org.example.taskproject.mapper.UserMapper;
import org.example.taskproject.repository.RoleRepository;
import org.example.taskproject.repository.UserRepository;
import org.example.taskproject.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void createAdmin(UserDtoRequest dto) {
        log.info("Creating admin user: {}", dto.getUsername());
        validateUser(dto);

        UserEntity user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        RoleEntity adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> {
                    log.error("Admin role not found");
                    return new NotFoundException("ROLE_NOT_FOUND");
                });

        user.setUserStatus(UserStatus.ACTIVE);
        user.setRoles(List.of(adminRole));
        userRepository.save(user);

        log.info("Admin user created successfully: {}", dto.getUsername());
    }

    private void validateUser(UserDtoRequest dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("Username already exists: {}", dto.getUsername());
            throw new AlreadyExistException("USERNAME_ALREADY_EXIST");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email already exists: {}", dto.getEmail());
            throw new AlreadyExistException("EMAIL_ALREADY_EXIST");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("Password mismatch for user: {}", dto.getUsername());
            throw new PasswordMismatchException("PASSWORD_MISMATCH");
        }
    }

    @Override
    @Transactional
    public void blockUser(Long id){
        log.info("Blocking user with id={}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        if (user.getUserStatus() == UserStatus.BLOCKED) {
            log.warn("User already blocked: id={}", id);
            throw new IllegalStateException("USER_ALREADY_BLOCKED");
        }

        user.setUserStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        log.info("User blocked successfully: id={}", id);
    }

    @Override
    @Transactional
    public void deleteUser(Long id){
        log.info("Deleting user with id={}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        if (user.getUserStatus() == UserStatus.DELETED) {
            log.warn("User already deleted: id={}", id);
            throw new IllegalStateException("USER_ALREADY_DELETED");
        }

        user.setUserStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("User deleted successfully: id={}", id);
    }

    @Override
    @Transactional
    public void activateUser(Long id){
        log.info("Activating user with id={}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        if (user.getUserStatus() == UserStatus.DELETED) {
            log.warn("Cannot activate deleted user: id={}", id);
            throw new IllegalStateException("USER_DELETED");
        }

        if(user.getUserStatus() == UserStatus.ACTIVE){
            log.warn("User already active: id={}", id);
            throw new IllegalStateException("USER_ALREADY_ACTIVE");
        }

        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User activated successfully: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDtoResponse> getAllUsers(int page, int size) {
        log.info("Fetching all users: page={}, size={}", page, size);
        return userRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        ).map(userMapper::toDto);
    }
}
