package org.astronatalai.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.astronatalai.backend.client.AstroEngineClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

class EngineHealthIndicatorTest {

    private final AstroEngineClient astroEngineClient = mock(AstroEngineClient.class);
    private final EngineHealthIndicator indicator =
            new EngineHealthIndicator(astroEngineClient, "http://engine.test");

    @Test
    void reportsUpWhenEngineResponds() {
        when(astroEngineClient.health()).thenReturn(true);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void reportsDownWhenEngineFails() {
        when(astroEngineClient.health()).thenReturn(false);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
    }
}
