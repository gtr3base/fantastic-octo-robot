package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.UserUpdateRequest;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.dto.responses.UserResponse;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    @Test
    @DisplayName("Get All Users - Success")
    void getAllUsers_Success() {
        UserResponse user1 = mock(UserResponse.class);
        when(userService.getAllUsers()).thenReturn(List.of(user1));

        ResponseEntity<List<UserResponse>> response = adminUserController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Get User By ID - Success")
    void getUserById_Success() {
        UserResponse mockResponse = mock(UserResponse.class);
        when(userService.getUserById(1L)).thenReturn(mockResponse);

        ResponseEntity<UserResponse> response = adminUserController.getUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("Update User - Success")
    void updateUser_Success() {
        UserUpdateRequest request = mock(UserUpdateRequest.class);
        UserResponse mockResponse = mock(UserResponse.class);

        when(userService.updateUser(1L, request)).thenReturn(mockResponse);

        ResponseEntity<UserResponse> response = adminUserController.updateUser(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(userService, times(1)).updateUser(1L, request);
    }

    @Test
    @DisplayName("Delete User - Success")
    void deleteUser_Success() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = adminUserController.deleteUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("Get User Cards - Success")
    void getUserCards_Success() {
        CardResponse card1 = mock(CardResponse.class);
        when(userService.getUserCards(1L)).thenReturn(List.of(card1));

        ResponseEntity<List<CardResponse>> response = adminUserController.getUserCards(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(userService, times(1)).getUserCards(1L);
    }
}