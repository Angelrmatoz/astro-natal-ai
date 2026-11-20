package org.astronatalai.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@SpringBootTest(properties = {
        "app.jwt.secret=test-only-secret-with-at-least-32-bytes",
        "app.jwt.ttl=PT24H",
        "app.jwt.issuer=astro-natal-ai",
        "app.geocoding.url=https://geo.test",
        "app.astro-engine.url=https://engine.test",
        "app.cors.allowed-origins=http://localhost:5173",
        "app.ai.api-key=test-key",
        "app.ai.model=test-model",
        "app.ai.fallback-model=test-fallback-model",
        "app.ai.timeout-seconds=60",
        "DB_HOST=localhost",
        "DB_PORT=5432",
        "POSTGRES_DB=test",
        "POSTGRES_USER=test",
        "POSTGRES_PASSWORD=test"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {
}
