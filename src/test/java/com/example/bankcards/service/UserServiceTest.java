package com.example.bankcards.service;

import com.example.bankcards.dto.requests.LoginRequest;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.responses.AuthResponse;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.UserRole;
import com.example.bankcards.exception.LoginException;
import com.example.bankcards.mappers.UserMapper;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John", "Doe", "john@test.com", "password123");

        mockUser = User.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .email("john@test.com")
                .password("encoded_password")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("Register - Success")
    void register_Success() {
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userMapper.toUser(registerRequest)).thenReturn(mockUser);
        doReturn("encoded_password").when(passwordEncoder).encode("encoded_password");

        when(jwtService.generateToken(1, "john@test.com", "USER")).thenReturn("jwt_access_token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("jwt_refresh_token");
        when(refreshTokenService.createRefToken("john@test.com")).thenReturn(mockRefreshToken);

        AuthResponse response = userService.register(registerRequest);

        assertThat(response.accessToken()).isEqualTo("jwt_access_token");
        assertThat(response.refreshToken()).isEqualTo("jwt_refresh_token");

        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("Register - Fails if Email Already Exists")
    void register_EmailTaken() {
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("is already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("john@test.com", "password123");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtService.generateToken(1, "john@test.com", "USER")).thenReturn("jwt_access_token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("jwt_refresh_token");
        when(refreshTokenService.createRefToken("jwt_access_token")).thenReturn(mockRefreshToken);

        AuthResponse response = userService.login(loginRequest);

        assertThat(response.accessToken()).isEqualTo("jwt_access_token");
        assertThat(response.refreshToken()).isEqualTo("jwt_refresh_token");
    }

    @Test
    @DisplayName("Login - Fails on Wrong Password")
    void login_WrongPassword() {
        LoginRequest loginRequest = new LoginRequest("john@test.com", "wrong_password");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Fails on Non-Existent User")
    void login_UserNotFound() {
        LoginRequest loginRequest = new LoginRequest("nobody@test.com", "password123");

        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
