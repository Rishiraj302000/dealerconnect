package com.dealerconnect.auth_service.service;

import com.dealerconnect.auth_service.dto.AuthResponse;
import com.dealerconnect.auth_service.dto.LoginRequest;
import com.dealerconnect.auth_service.dto.RegisterRequest;
import com.dealerconnect.auth_service.dto.RegisterResponse;
import com.dealerconnect.auth_service.entity.User;
import com.dealerconnect.auth_service.exception.EmailAlreadyExistsException;
import com.dealerconnect.auth_service.exception.InvalidCredentialsException;
import com.dealerconnect.auth_service.repository.UserRepository;
import com.dealerconnect.auth_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        User saved = userRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getName(), saved.getEmail(),
                saved.getRole(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return AuthResponse.bearer(token, user.getEmail(), user.getRole());
    }
}
