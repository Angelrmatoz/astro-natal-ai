package org.astronatalai.backend.client;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import org.astronatalai.backend.exception.AiUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GemmaClient {

    private final ModelInvoker invoker;
    private final String primaryModel;
    private final String fallbackModel;

    @Autowired
    public GemmaClient(@Value("${app.ai.api-key}") String apiKey,
                       @Value("${app.ai.model}") String primaryModel,
                       @Value("${app.ai.fallback-model}") String fallbackModel,
                       @Value("${app.ai.timeout-seconds}") int timeoutSeconds) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GOOGLE_AI_API_KEY es obligatoria");
        }
        if (primaryModel == null || primaryModel.isBlank()) {
            throw new IllegalStateException("GOOGLE_AI_MODEL es obligatorio");
        }
        if (fallbackModel == null || fallbackModel.isBlank()) {
            throw new IllegalStateException("GOOGLE_AI_FALLBACK_MODEL es obligatorio");
        }
        HttpOptions httpOptions = HttpOptions.builder()
                .timeout(timeoutSeconds)
                .retryOptions(HttpRetryOptions.builder().attempts(1).build())
                .build();
        Client client = Client.builder()
                .apiKey(apiKey)
                .httpOptions(httpOptions)
                .build();
        this.invoker = (model, prompt) -> client.models
                .generateContent(model, prompt, null)
                .text();
        this.primaryModel = primaryModel;
        this.fallbackModel = fallbackModel;
    }

    GemmaClient(ModelInvoker invoker, String primaryModel, String fallbackModel) {
        this.invoker = invoker;
        this.primaryModel = primaryModel;
        this.fallbackModel = fallbackModel;
    }

    public GeneratedText generate(String prompt) {
        try {
            return new GeneratedText(requireText(invoker.invoke(primaryModel, prompt)), primaryModel);
        } catch (RuntimeException primaryFailure) {
            try {
                return new GeneratedText(requireText(invoker.invoke(fallbackModel, prompt)), fallbackModel);
            } catch (RuntimeException fallbackFailure) {
                primaryFailure.addSuppressed(fallbackFailure);
                throw new AiUnavailableException(
                        "El servicio de IA no está disponible", primaryFailure);
            }
        }
    }

    private static String requireText(String text) {
        if (text == null || text.isBlank()) {
            throw new AiUnavailableException("El modelo de IA devolvió una respuesta vacía");
        }
        return text.trim();
    }

    public record GeneratedText(String text, String model) {
    }

    @FunctionalInterface
    interface ModelInvoker {
        String invoke(String model, String prompt);
    }
}
