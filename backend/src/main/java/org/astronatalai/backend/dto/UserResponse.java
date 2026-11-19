package org.astronatalai.backend.dto;

import java.util.UUID;
import org.astronatalai.backend.entity.User;

public record UserResponse(UUID id, String email, String nombre) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNombre());
    }
}
