package com.example.bankcards.service;

import com.example.bankcards.dto.requests.LoginRequest;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.requests.UserUpdateRequest;
import com.example.bankcards.dto.responses.AuthResponse;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.dto.responses.UserResponse;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.LoginException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.mappers.CardMapper;
import com.example.bankcards.mappers.UserMapper;
import com.example.bankcards.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private static final String USER_ALREADY_EXISTS_MSG = "Login %s is already in use";
    private static final String INVALID_CREDS_MSG = "Invalid credentials";
    private static final String USER_EMAIL_TAKEN = "User email taken";
    private static final String USER_NOT_FOUND = "User %s not found";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final CardMapper cardMapper;

    public UserService(UserRepository userRepository, JwtService jwtService, UserMapper userMapper, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, CardMapper cardMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.cardMapper = cardMapper;
    }

    public AuthResponse register(RegisterRequest req) {
        log.info("Registering user: {}", req.email());
        String email = req.email().trim();

        if(userRepository.existsByEmail(email)) {
            log.warn("User with email {} already exists", email);
            throw new LoginException(String.format(USER_ALREADY_EXISTS_MSG, email));
        }
        User user = convertToEntity(req);
        log.info("Password before encoding: {}", user.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Password after encoding: {}", user.getPassword());

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId().intValue(),email,user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefToken(email);

        log.info("User with email {} registered successfully", email);

        return new AuthResponse(token, refreshToken.getToken());
    }

    public AuthResponse login(LoginRequest req){
        log.info("Logging in user: {}", req.email());
        var user = userRepository.findByEmail(req.email().trim())
                .orElseThrow(() -> new BadCredentialsException(INVALID_CREDS_MSG));

        if(!passwordEncoder.matches(req.password(), user.getPassword())) {
            log.warn("Invalid credentials");
            throw new BadCredentialsException(INVALID_CREDS_MSG);
        }

        String loginToken = jwtService.generateToken(user.getId().intValue(),user.getEmail(),user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefToken(req.email());

        if(refreshToken == null){
            log.warn("Invalid refresh token");
            refreshToken = refreshTokenService.createRefToken(user.getEmail());
        }

        return new AuthResponse(loginToken, refreshToken.getToken());
    }

    private User convertToEntity(RegisterRequest req) {
        log.info("Converting user: {}", req.email());
        return userMapper.toUser(req);
    }

    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long userId) {
        log.info("Getting user with id: {}", userId);
        return userRepository.getUsersById(userId);
    }

    public UserResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        log.info("Updating user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, userId)));

        if(userUpdateRequest.name() != null) {
            log.info("Updating users name: {}", userUpdateRequest.name());
            user.setName(userUpdateRequest.name());
        }
        if(userUpdateRequest.surname() != null) {
            log.info("Updating users surname: {}", userUpdateRequest.surname());
            user.setSurname(userUpdateRequest.surname());
        }
        if(userUpdateRequest.email() != null) {
            if(!user.getEmail().equals(userUpdateRequest.email()) &&
                userRepository.existsByEmail(user.getEmail())) {
                log.warn("User with email {} already exists here", user.getEmail());
                throw new UserOperationException(USER_EMAIL_TAKEN);
            }
            log.info("Updating users email: {}", user.getEmail());
            user.setEmail(userUpdateRequest.email());
        }
        if(userUpdateRequest.password() != null) {
            log.info("Updating users password: {}", userUpdateRequest.password());
            user.setPassword(passwordEncoder.encode(userUpdateRequest.password()));
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user with id: {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    public List<CardResponse> getUserCards(Long userId) {
        log.info("Getting cards for user with id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, userId)))
                .getCards()
                .stream()
                .map(cardMapper::toCardResponse)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);
        userRepository.deleteById(userId);
    }

    public Long getCurrentUserId(Principal principal){
        log.info("Getting current user id: {}", principal.getName());
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, principal.getName())));
        return user.getId();
    }
}
