package org.astronatalai.backend.service;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.dto.UserResponse;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.exception.InvalidProfileRequestException;
import org.astronatalai.backend.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse update(UUID userId, String nombre, String timezone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        user.rename(nombre.trim());
        if (timezone != null && !timezone.isBlank()) {
            user.updateTimezone(validateTimezone(timezone.trim()));
        }
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    private static String validateTimezone(String timezone) {
        try {
            return ZoneId.of(timezone).getId();
        } catch (DateTimeException exception) {
            throw new InvalidProfileRequestException(
                    "La zona horaria no es IANA válida: " + timezone);
        }
    }
}
