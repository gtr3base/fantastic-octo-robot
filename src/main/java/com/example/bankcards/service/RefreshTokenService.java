package com.example.bankcards.service;

import com.example.bankcards.dto.requests.RefreshTokenRequest;
import com.example.bankcards.dto.responses.AuthResponse;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.LoginException;
import com.example.bankcards.exception.TokenRefreshException;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private static final String REFRESH_TOKEN_EXPIRED = "Refresh token expired";
    private static final String LOGIN_NOT_FOUND = "Email %s not found";
    private static final String TOKEN_NOT_FOUND = "Token not found";

    public RefreshTokenService(JwtService jwtService, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RefreshToken createRefToken(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginException(String.format(LOGIN_NOT_FOUND, email)));

        refreshTokenRepository.findByUserEmail(email).ifPresent(existingToken -> {
            refreshTokenRepository.delete(existingToken);
            refreshTokenRepository.flush();
        });

        RefreshToken rToken = RefreshToken
                .builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        return refreshTokenRepository.save(rToken);
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),REFRESH_TOKEN_EXPIRED);
        }
        return token;
    }

    public AuthResponse processRefreshToken(RefreshTokenRequest refReq){
        return findByToken(refReq.token())
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user.getId().intValue(), user.getEmail(), user.getRole().name());
                    return new AuthResponse(token, refReq.token());
                })
                .orElseThrow(() -> new TokenRefreshException(refReq.token(), REFRESH_TOKEN_EXPIRED));
    }
}
