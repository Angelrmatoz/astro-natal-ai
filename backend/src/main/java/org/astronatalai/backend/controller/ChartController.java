package org.astronatalai.backend.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.dto.ChartResponse;
import org.astronatalai.backend.dto.CreateChartRequest;
import org.astronatalai.backend.service.ChartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    @PostMapping
    public ChartResponse save(@AuthenticationPrincipal Jwt jwt,
                              @Valid @RequestBody CreateChartRequest request) {
        return chartService.save(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping("/me")
    public ChartResponse me(@AuthenticationPrincipal Jwt jwt) {
        return chartService.me(UUID.fromString(jwt.getSubject()));
    }
}
