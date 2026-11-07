package org.astronatalai.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.astronatalai.backend.dto.AuthResponse;
import org.astronatalai.backend.dto.LoginRequest;
import org.astronatalai.backend.dto.RegisterRequest;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.exception.EmailAlreadyUsedException;
import org.astronatalai.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "ana@example.com";
    private static final String PASSWORD = "secret123";
    private static final String HASH = "$2a$10$hash";
    private static final JwtService.IssuedToken TOKEN = new JwtService.IssuedToken(
            "token", Instant.parse("2026-11-06T00:00:00Z"));

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerNormalizesEmailHashesPasswordAndReturnsToken() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASH);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.issue(any(UUID.class), eq(EMAIL))).thenReturn(TOKEN);

        AuthResponse response = authService.register(
                new RegisterRequest("  Ana@Example.COM ", PASSWORD, " Ana "));

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo(EMAIL);
        assertThat(response.user().nombre()).isEqualTo("Ana");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getValue().getPasswordHash()).isEqualTo(HASH);
        assertThat(saved.getValue().getNombre()).isEqualTo("Ana");
    }

    @Test
    void registerRejectsExistingEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest(EMAIL, PASSWORD, "Ana")))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void loginReturnsTokenWhenPasswordMatches() {
        User user = new User(EMAIL, HASH, "Ana");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
        when(jwtService.issue(user.getId(), EMAIL)).thenReturn(TOKEN);

        AuthResponse response = authService.login(new LoginRequest("Ana@Example.COM", PASSWORD));

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.user().id()).isEqualTo(user.getId());
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = new User(EMAIL, HASH, "Ana");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).issue(any(UUID.class), any());
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
    }
}
