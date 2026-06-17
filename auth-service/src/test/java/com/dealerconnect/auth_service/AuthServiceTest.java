package com.dealerconnect.auth_service;

import com.dealerconnect.auth_service.dto.AuthResponse;
import com.dealerconnect.auth_service.dto.LoginRequest;
import com.dealerconnect.auth_service.dto.RegisterRequest;
import com.dealerconnect.auth_service.dto.RegisterResponse;
import com.dealerconnect.auth_service.entity.User;
import com.dealerconnect.auth_service.enums.Role;
import com.dealerconnect.auth_service.exception.EmailAlreadyExistsException;
import com.dealerconnect.auth_service.exception.InvalidCredentialsException;
import com.dealerconnect.auth_service.repository.UserRepository;
import com.dealerconnect.auth_service.security.JwtService;
import com.dealerconnect.auth_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService using Mockito - the repository, password encoder and JWT
 * service are mocked so only the service's business logic is exercised.
 */
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_savesUser_andReturnsResponse() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@dc.com", "secret1", Role.ADMIN);
        when(userRepository.existsByEmail("alice@dc.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("hashed");
        User saved = User.builder().id(1L).name("Alice").email("alice@dc.com")
                .password("hashed").role(Role.ADMIN).createdAt(LocalDateTime.now()).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        RegisterResponse response = authService.register(request);

        assertEquals(1L, response.id());
        assertEquals("alice@dc.com", response.email());
        assertEquals(Role.ADMIN, response.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_whenEmailExists_throwsConflict() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@dc.com", "secret1", Role.ADMIN);
        when(userRepository.existsByEmail("alice@dc.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("alice@dc.com", "secret1");
        User user = User.builder().id(1L).email("alice@dc.com").password("hashed").role(Role.ADMIN).build();
        when(userRepository.findByEmail("alice@dc.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret1", "hashed")).thenReturn(true);
        when(jwtService.generateToken("alice@dc.com", Role.ADMIN)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.type());
        assertEquals(Role.ADMIN, response.role());
    }

    @Test
    void login_withWrongPassword_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("alice@dc.com", "wrong");
        User user = User.builder().email("alice@dc.com").password("hashed").role(Role.ADMIN).build();
        when(userRepository.findByEmail("alice@dc.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_whenUserNotFound_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("missing@dc.com", "secret1");
        when(userRepository.findByEmail("missing@dc.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
