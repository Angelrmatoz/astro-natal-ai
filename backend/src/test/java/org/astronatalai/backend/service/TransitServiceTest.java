package org.astronatalai.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.astronatalai.backend.client.AstroEngineClient;
import org.astronatalai.backend.entity.BirthChart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class TransitServiceTest {

    @Mock
    private ChartService chartService;

    @Mock
    private AstroEngineClient astroEngineClient;

    private TransitService transitService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC);
        transitService = new TransitService(chartService, astroEngineClient, clock);
    }

    @Test
    void requestsTransitsForTodayUsingTheStoredChart() {
        UUID userId = UUID.randomUUID();
        BirthChart chart = mock(BirthChart.class);
        JsonNode chartData = JsonMapper.builder().build().readTree("""
                {"planets": [{"name": "Sun", "sign": "Taurus"}]}
                """);
        JsonNode transits = JsonMapper.builder().build().readTree("""
                {"aspects": [{"type": "square"}]}
                """);

        when(chartService.getByUserId(userId)).thenReturn(chart);
        when(chart.getDatosCarta()).thenReturn(chartData);
        when(astroEngineClient.transits(chartData, LocalDate.of(2026, 11, 6))).thenReturn(transits);

        assertThat(transitService.current(userId)).isEqualTo(transits);
    }
}
