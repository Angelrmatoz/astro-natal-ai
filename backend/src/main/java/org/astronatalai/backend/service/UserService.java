package org.astronatalai.backend.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.dto.UserResponse;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse rename(UUID userId, String nombre) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        user.rename(nombre.trim());
        return UserResponse.from(userRepository.saveAndFlush(user));
    }
}
