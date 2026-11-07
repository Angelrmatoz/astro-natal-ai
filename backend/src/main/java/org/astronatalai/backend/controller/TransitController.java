package org.astronatalai.backend.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.service.TransitService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/transits")
@RequiredArgsConstructor
public class TransitController {

    private final TransitService transitService;

    @GetMapping("/current")
    public JsonNode current(@AuthenticationPrincipal Jwt jwt) {
        return transitService.current(UUID.fromString(jwt.getSubject()));
    }
}
