package org.astronatalai.backend.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.dto.AuthResponse;
import org.astronatalai.backend.dto.LoginRequest;
import org.astronatalai.backend.dto.RegisterRequest;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.exception.EmailAlreadyUsedException;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return responseFor(user);
    }

    private AuthResponse responseFor(User user) {
        JwtService.IssuedToken token = jwtService.issue(user.getId(), user.getEmail());
        return new AuthResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                new AuthResponse.User(user.getId(), user.getEmail(), user.getNombre()));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
