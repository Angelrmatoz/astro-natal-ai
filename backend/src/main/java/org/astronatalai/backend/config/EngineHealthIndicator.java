package org.astronatalai.backend.config;

import org.astronatalai.backend.client.AstroEngineClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class EngineHealthIndicator implements HealthIndicator {

    private final AstroEngineClient astroEngineClient;
    private final String url;

    public EngineHealthIndicator(AstroEngineClient astroEngineClient,
                                 @Value("${app.astro-engine.url}") String url) {
        this.astroEngineClient = astroEngineClient;
        this.url = url;
    }

    @Override
    public Health health() {
        boolean available = astroEngineClient.health();
        return (available ? Health.up() : Health.down())
                .withDetail("url", url)
                .build();
    }
}
