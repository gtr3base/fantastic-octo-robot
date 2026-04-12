package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.LoginRequest;
import com.example.bankcards.dto.requests.RefreshTokenRequest;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.responses.AuthResponse;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public UserController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest req){
        AuthResponse authResponse = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req){
        AuthResponse authResponse = userService.login(req);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest req){
        AuthResponse response = refreshTokenService.processRefreshToken(req);

        return ResponseEntity.ok(response);
    }
}
