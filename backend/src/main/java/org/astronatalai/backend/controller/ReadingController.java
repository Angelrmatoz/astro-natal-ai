package org.astronatalai.backend.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.dto.CreateReadingRequest;
import org.astronatalai.backend.dto.ReadingResponse;
import org.astronatalai.backend.service.ReadingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;

    @PostMapping
    public ReadingResponse generate(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody CreateReadingRequest request) {
        return readingService.generate(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping("/current")
    public ReadingResponse current(@AuthenticationPrincipal Jwt jwt,
                                   @RequestParam UUID chartId,
                                   @RequestParam String type) {
        return readingService.current(UUID.fromString(jwt.getSubject()), chartId, type);
    }
}
