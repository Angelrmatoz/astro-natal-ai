package org.astronatalai.backend.service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.dto.AuthResponse;
import org.astronatalai.backend.dto.LoginRequest;
import org.astronatalai.backend.dto.RegisterRequest;
import org.astronatalai.backend.dto.UserResponse;
import org.astronatalai.backend.entity.AuthSession;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.exception.EmailAlreadyUsedException;
import org.astronatalai.backend.repository.AuthSessionRepository;
import org.astronatalai.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        User user = new User(email, passwordEncoder.encode(request.password()), request.nombre().trim());
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyUsedException(email, ex);
        }
        return responseFor(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientKey) {
        String email = normalizeEmail(request.email());
        String limiterKey = email + "|" + clientKey;
        loginRateLimiter.check(limiterKey);

        Optional<User> found = userRepository.findByEmail(email);
        if (found.isEmpty() || !passwordEncoder.matches(request.password(), found.get().getPasswordHash())) {
            loginRateLimiter.recordFailure(limiterKey);
            throw new BadCredentialsException("Credenciales inválidas");
        }

        loginRateLimiter.reset(limiterKey);
        return responseFor(found.get());
    }

    @Transactional
    public void logout(String tokenId) {
        authSessionRepository.findByTokenId(tokenId)
                .ifPresent(session -> session.revoke(Instant.now()));
    }

    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
    }

    private AuthResponse responseFor(User user) {
        JwtService.IssuedToken token = jwtService.issue(user.getId(), user.getEmail());
        authSessionRepository.save(new AuthSession(user, token.id(), token.expiresAt()));
        return new AuthResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                new AuthResponse.User(
                        user.getId(), user.getEmail(), user.getNombre(), user.getTimezone()));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
