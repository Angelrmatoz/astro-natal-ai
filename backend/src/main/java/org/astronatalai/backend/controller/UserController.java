package org.astronatalai.backend.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.dto.UpdateProfileRequest;
import org.astronatalai.backend.dto.UserResponse;
import org.astronatalai.backend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/me")
    public UserResponse update(@AuthenticationPrincipal Jwt jwt,
                               @Valid @RequestBody UpdateProfileRequest request) {
        return userService.rename(UUID.fromString(jwt.getSubject()), request.nombre());
    }
}
