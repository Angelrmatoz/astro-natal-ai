package org.astronatalai.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtServiceTest {

    private static final SecretKey KEY = new SecretKeySpec(
            "test-secret-with-at-least-32-bytes-for-hs256".getBytes(StandardCharsets.UTF_8),
            "HmacSHA256");

    private final JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(KEY).build();
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(KEY).build();

    @Test
    void issuesTokenWithSubjectEmailIssuerAndExpiry() {
        JwtService service = new JwtService(encoder, Duration.ofHours(1), "test-issuer");
        UUID userId = UUID.randomUUID();

        JwtService.IssuedToken issued = service.issue(userId, "ana@example.com");
        Jwt jwt = decoder.decode(issued.value());

        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaimAsString("email")).isEqualTo("ana@example.com");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("test-issuer");
        assertThat(issued.expiresAt()).isAfter(Instant.now().plusSeconds(59 * 60));
    }

    @Test
    void tokenExpiresAfterConfiguredTtl() {
        JwtService service = new JwtService(encoder, Duration.ofHours(2), "test-issuer");

        JwtService.IssuedToken issued = service.issue(UUID.randomUUID(), "ana@example.com");
        Jwt jwt = decoder.decode(issued.value());

        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isNotNull();
        assertThat(jwt.getExpiresAt().getEpochSecond())
                .isEqualTo(jwt.getIssuedAt().plus(Duration.ofHours(2)).getEpochSecond());
        assertThat(issued.expiresAt().getEpochSecond()).isEqualTo(jwt.getExpiresAt().getEpochSecond());
    }
}
