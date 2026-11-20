package org.astronatalai.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.client.GeocodingClient;
import org.astronatalai.backend.dto.GeocodingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingClient geocodingClient;

    @GetMapping("/search")
    public List<GeocodingResult> search(@RequestParam("q") String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.length() < 2) {
            return List.of();
        }
        return geocodingClient.search(trimmed);
    }
}
