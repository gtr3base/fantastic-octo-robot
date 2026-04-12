package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.LoginRequest;
import com.example.bankcards.dto.requests.RefreshTokenRequest;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.responses.AuthResponse;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("Register User - Success")
    void registerUser_Success() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "test@test.com", "pass123");
        AuthResponse mockResponse = mock(AuthResponse.class);

        when(userService.register(request)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = userController.registerUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(userService, times(1)).register(request);
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        LoginRequest request = new LoginRequest("test@test.com", "pass123");
        AuthResponse mockResponse = mock(AuthResponse.class);

        when(userService.login(request)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = userController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(userService, times(1)).login(request);
    }

    @Test
    @DisplayName("Refresh Token - Success")
    void refreshToken_Success() {
        RefreshTokenRequest request = mock(RefreshTokenRequest.class);
        AuthResponse mockResponse = mock(AuthResponse.class);

        when(refreshTokenService.processRefreshToken(request)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = userController.refreshToken(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(refreshTokenService, times(1)).processRefreshToken(request);
    }
}