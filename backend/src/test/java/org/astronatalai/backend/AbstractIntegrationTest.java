package org.astronatalai.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
        "app.jwt.secret=test-only-secret-with-at-least-32-bytes",
        "app.jwt.ttl=PT24H",
        "app.jwt.issuer=astro-natal-ai",
        "app.geocoding.url=https://geo.test",
        "app.astro-engine.url=https://engine.test",
        "DB_HOST=localhost",
        "DB_PORT=5432",
        "POSTGRES_DB=test",
        "POSTGRES_USER=test",
        "POSTGRES_PASSWORD=test",
        "BACKEND_PORT=0"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {
}
