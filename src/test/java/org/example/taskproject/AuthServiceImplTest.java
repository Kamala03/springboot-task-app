package org.example.taskproject;

import jakarta.servlet.http.HttpServletRequest;
import org.example.taskproject.dto.*;
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
import org.example.taskproject.service.impl.*;
import org.example.taskproject.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private OtpServiceImpl otpService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ResetTokenServiceImpl resetTokenService;
    @Mock
    private BlacklistedTokenServiceImpl blacklistedTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== SIGN UP ====================
    @Test
    void signUp_shouldRegisterUserSuccessfully() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("user");
        dto.setEmail("user@test.com");
        dto.setPassword("pass123");
        dto.setConfirmPassword("pass123");

        UserEntity userEntity = new UserEntity();
        RoleEntity role = new RoleEntity();
        role.setName(RoleName.USER);

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(userEntity);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(role));
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        authService.signUp(dto);

        assertEquals(UserStatus.ACTIVE, userEntity.getUserStatus());
        assertEquals(List.of(role), userEntity.getRoles());
        assertEquals("encodedPass", userEntity.getPassword());
        verify(userRepository).save(userEntity);
    }

    @Test
    void signUp_shouldThrowAlreadyExistException_whenUsernameExists() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("user");
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);

        AlreadyExistException ex = assertThrows(AlreadyExistException.class, () -> authService.signUp(dto));
        assertEquals("USERNAME_ALREADY_EXIST", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_shouldThrowAlreadyExistException_whenEmailExists() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("user");
        dto.setEmail("test@test.com");
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        AlreadyExistException ex = assertThrows(AlreadyExistException.class, () -> authService.signUp(dto));
        assertEquals("EMAIL_ALREADY_EXIST", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_shouldThrowPasswordMismatchException() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("user");
        dto.setPassword("pass1");
        dto.setConfirmPassword("pass2");

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        PasswordMismatchException ex = assertThrows(PasswordMismatchException.class, () -> authService.signUp(dto));
        assertEquals("PASSWORD_MISMATCH", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_shouldThrowNotFoundException_whenRoleMissing() {
        UserDtoRequest dto = new UserDtoRequest();
        dto.setUsername("user");
        dto.setPassword("pass");
        dto.setConfirmPassword("pass");

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(new UserEntity());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> authService.signUp(dto));
        assertEquals("ROLE_NOT_FOUND", ex.getMessage());
    }

    // ==================== SIGN IN ====================
    @Test
    void signIn_shouldSendOtpForValidUser() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("pass");

        // Use Spring Security User for testing
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        request.getEmail(),
                        "encodedPass", // encoded password
                        List.of() // authorities
                );

        when(customUserDetailsService.loadUserByUsername(request.getEmail())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.getPassword(), userDetails.getPassword())).thenReturn(true);

        authService.signIn(request);

        verify(otpService).sendOtp(request.getEmail());
    }

    @Test
    void signIn_shouldThrowBadCredentialsException_forInvalidPassword() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");

        // Use Spring Security User for testing
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        request.getEmail(),
                        "encodedPass", // encoded password
                        List.of() // authorities
                );

        when(customUserDetailsService.loadUserByUsername(request.getEmail())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.getPassword(), userDetails.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.signIn(request));

        // OTP should NOT be sent
        verify(otpService, never()).sendOtp(anyString());
    }


    // ==================== VERIFY OTP FOR SIGN IN ====================
    @Test
    void verifyOtpForSignIn_shouldReturnTokenForValidOtp() {
        OtpDto otpDto = new OtpDto();
        otpDto.setEmail("user@test.com");
        otpDto.setCode("1234");

        when(otpService.checkOtp(otpDto.getEmail(), otpDto.getCode())).thenReturn(true);
        when(jwtUtil.generateToken(otpDto.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.verifyOtpForSignIn(otpDto);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void verifyOtpForSignIn_shouldThrowBadCredentials_forInvalidOtp() {
        OtpDto otpDto = new OtpDto();
        otpDto.setEmail("user@test.com");
        otpDto.setCode("wrong");

        when(otpService.checkOtp(otpDto.getEmail(), otpDto.getCode())).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> authService.verifyOtpForSignIn(otpDto));
    }

    // ==================== LOGOUT ====================
    @Test
    void logout_shouldBlacklistToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(jwtUtil.resolveToken(request)).thenReturn("token");
        when(jwtUtil.getJti("token")).thenReturn("jti123");
        when(jwtUtil.getExpiration("token")).thenReturn(Instant.now().plusSeconds(3600));

        authService.logout(request);

        verify(blacklistedTokenService).blacklist(eq("jti123"), any());
    }
}
