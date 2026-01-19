package org.example.taskproject;

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
import org.example.taskproject.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserServiceImplTest {

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAdmin_shouldCreateAdminSuccessfully() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("admin");
        dto.setEmail("admin@test.com");
        dto.setPassword("pass123");
        dto.setConfirmPassword("pass123");

        UserEntity userEntity = new UserEntity();
        RoleEntity adminRole = new RoleEntity();
        adminRole.setName(RoleName.ADMIN);

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(userEntity);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        adminUserService.createAdmin(dto);

        assertEquals(UserStatus.ACTIVE, userEntity.getUserStatus());
        assertEquals(List.of(adminRole), userEntity.getRoles());
        assertEquals("encodedPass", userEntity.getPassword());
        verify(userRepository).save(userEntity);
    }

    @Test
    void createAdmin_shouldThrowAlreadyExistException_whenUsernameExists() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("admin");

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);

        AlreadyExistException ex = assertThrows(AlreadyExistException.class,
                () -> adminUserService.createAdmin(dto));

        assertEquals("USERNAME_ALREADY_EXIST", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createAdmin_shouldThrowAlreadyExistException_whenEmailExists() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("admin");
        dto.setEmail("test@test.com");

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        AlreadyExistException ex = assertThrows(AlreadyExistException.class,
                () -> adminUserService.createAdmin(dto));

        assertEquals("EMAIL_ALREADY_EXIST", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createAdmin_shouldThrowPasswordMismatchException() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("admin");
        dto.setPassword("pass1");
        dto.setConfirmPassword("pass2");

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        PasswordMismatchException ex = assertThrows(PasswordMismatchException.class,
                () -> adminUserService.createAdmin(dto));

        assertEquals("PASSWORD_MISMATCH", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createAdmin_shouldThrowNotFoundException_whenRoleNotFound() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("admin");
        dto.setPassword("pass");
        dto.setConfirmPassword("pass");

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(new UserEntity());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> adminUserService.createAdmin(dto));

        assertEquals("ROLE_NOT_FOUND", ex.getMessage());
    }


    @Test
    void blockUser_shouldBlockUserSuccessfully() {
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setUserStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        adminUserService.blockUser(userId);

        assertEquals(UserStatus.BLOCKED, user.getUserStatus());
    }

    @Test
    void blockUser_shouldThrowException_whenAlreadyBlocked() {
        UserEntity user = new UserEntity();
        user.setUserStatus(UserStatus.BLOCKED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adminUserService.blockUser(1L));

        assertEquals("USER_ALREADY_BLOCKED", ex.getMessage());
    }


    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setUserStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        adminUserService.deleteUser(userId);

        assertEquals(UserStatus.DELETED, user.getUserStatus());
    }

    @Test
    void deleteUser_shouldThrowException_whenAlreadyDeleted() {
        UserEntity user = new UserEntity();
        user.setUserStatus(UserStatus.DELETED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adminUserService.deleteUser(1L));

        assertEquals("USER_ALREADY_DELETED", ex.getMessage());
    }


    @Test
    void activateUser_shouldActivateSuccessfully() {
        UserEntity user = new UserEntity();
        user.setUserStatus(UserStatus.BLOCKED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        adminUserService.activateUser(1L);

        assertEquals(UserStatus.ACTIVE, user.getUserStatus());
    }

    @Test
    void activateUser_shouldThrowException_whenDeleted() {
        UserEntity user = new UserEntity();
        user.setUserStatus(UserStatus.DELETED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adminUserService.activateUser(1L));

        assertEquals("USER_DELETED", ex.getMessage());
    }

    @Test
    void activateUser_shouldThrowException_whenAlreadyActive() {
        UserEntity user = new UserEntity();
        user.setUserStatus(UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adminUserService.activateUser(1L));

        assertEquals("USER_ALREADY_ACTIVE", ex.getMessage());
    }


    @Test
    void getAllUsers_shouldReturnPagedUsers() {
        UserEntity user1 = new UserEntity();
        UserEntity user2 = new UserEntity();
        UserDtoResponse dto1 = new UserDtoResponse();
        UserDtoResponse dto2 = new UserDtoResponse();

        Page<UserEntity> page = new PageImpl<>(List.of(user1, user2));

        when(userRepository.findAll(PageRequest.of(0, 2, Sort.by("id").descending()))).thenReturn(page);
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        Page<UserDtoResponse> result = adminUserService.getAllUsers(0, 2);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(dto1));
        assertTrue(result.getContent().contains(dto2));
    }
}
