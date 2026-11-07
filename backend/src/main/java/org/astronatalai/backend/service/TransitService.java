package org.astronatalai.backend.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.client.AstroEngineClient;
import org.astronatalai.backend.entity.BirthChart;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class TransitService {

    private final ChartService chartService;
    private final AstroEngineClient astroEngineClient;
    private final Clock clock;

    public JsonNode current(UUID userId) {
        BirthChart chart = chartService.getByUserId(userId);
        return astroEngineClient.transits(chart.getDatosCarta(), LocalDate.now(clock));
    }
}
