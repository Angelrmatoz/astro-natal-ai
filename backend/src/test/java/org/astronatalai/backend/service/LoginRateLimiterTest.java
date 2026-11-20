package org.astronatalai.backend.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.astronatalai.backend.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

class LoginRateLimiterTest {

    private static final String KEY = "ana@example.com|127.0.0.1";

    private final LoginRateLimiter limiter = new LoginRateLimiter(
            Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void blocksAfterFiveFailures() {
        for (int attempt = 0; attempt < 5; attempt++) {
            limiter.check(KEY);
            limiter.recordFailure(KEY);
        }

        assertThatThrownBy(() -> limiter.check(KEY))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void resetClearsTheCounter() {
        for (int attempt = 0; attempt < 5; attempt++) {
            limiter.recordFailure(KEY);
        }

        limiter.reset(KEY);

        assertThatCode(() -> limiter.check(KEY)).doesNotThrowAnyException();
    }
}
