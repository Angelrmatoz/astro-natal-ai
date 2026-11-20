package org.astronatalai.backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.astronatalai.backend.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

/**
 * Limita los intentos de login por correo e IP. En memoria: sirve para una instancia.
 * ponytail: si el backend escala a varias instancias, mover el contador a Redis.
 */
@Component
public class LoginRateLimiter {

    static final int MAX_ATTEMPTS = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private final Clock clock;
    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void check(String key) {
        Instant now = clock.instant();
        Deque<Instant> window = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (window) {
            prune(window, now);
            if (window.size() >= MAX_ATTEMPTS) {
                throw new TooManyRequestsException(
                        "Demasiados intentos de acceso. Espera unos minutos e inténtalo de nuevo.");
            }
        }
    }

    public void recordFailure(String key) {
        Instant now = clock.instant();
        Deque<Instant> window = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (window) {
            prune(window, now);
            window.addLast(now);
        }
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private static void prune(Deque<Instant> window, Instant now) {
        Instant cutoff = now.minus(WINDOW);
        while (!window.isEmpty() && window.peekFirst().isBefore(cutoff)) {
            window.removeFirst();
        }
    }
}
