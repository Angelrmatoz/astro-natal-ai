package org.astronatalai.backend.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.astronatalai.backend.exception.AiUnavailableException;
import org.junit.jupiter.api.Test;

class GemmaClientTest {

    private static final String PRIMARY = "gemma-4-31b-it";
    private static final String FALLBACK = "gemma-4-26b-a4b-it";

    @Test
    void usesPrimaryModelWhenItWorks() {
        List<String> calls = new ArrayList<>();
        GemmaClient client = new GemmaClient((model, prompt) -> {
            calls.add(model);
            return "lectura";
        }, PRIMARY, FALLBACK);

        GemmaClient.GeneratedText result = client.generate("prompt");

        assertThat(result.text()).isEqualTo("lectura");
        assertThat(result.model()).isEqualTo(PRIMARY);
        assertThat(calls).containsExactly(PRIMARY);
    }

    @Test
    void fallsBackToSecondaryModelWhenPrimaryFails() {
        List<String> calls = new ArrayList<>();
        GemmaClient client = new GemmaClient((model, prompt) -> {
            calls.add(model);
            if (PRIMARY.equals(model)) {
                throw new RuntimeException("primary unavailable");
            }
            return "lectura fallback";
        }, PRIMARY, FALLBACK);

        GemmaClient.GeneratedText result = client.generate("prompt");

        assertThat(result.text()).isEqualTo("lectura fallback");
        assertThat(result.model()).isEqualTo(FALLBACK);
        assertThat(calls).containsExactly(PRIMARY, FALLBACK);
    }

    @Test
    void fallsBackWhenPrimaryReturnsEmptyText() {
        GemmaClient client = new GemmaClient((model, prompt) ->
                PRIMARY.equals(model) ? " " : "lectura fallback", PRIMARY, FALLBACK);

        GemmaClient.GeneratedText result = client.generate("prompt");

        assertThat(result.text()).isEqualTo("lectura fallback");
        assertThat(result.model()).isEqualTo(FALLBACK);
    }

    @Test
    void failsWhenBothModelsFail() {
        GemmaClient client = new GemmaClient((model, prompt) -> {
            throw new RuntimeException("down");
        }, PRIMARY, FALLBACK);

        assertThatThrownBy(() -> client.generate("prompt"))
                .isInstanceOf(AiUnavailableException.class);
    }
}
