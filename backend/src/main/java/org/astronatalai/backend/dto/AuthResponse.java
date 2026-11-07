package org.astronatalai.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(String token, String tokenType, Instant expiresAt, User user) {

    public record User(UUID id, String email, String nombre) {
    }
}
