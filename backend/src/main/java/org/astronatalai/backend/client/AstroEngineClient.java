package org.astronatalai.backend.client;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.astronatalai.backend.dto.NatalChartCommand;
import org.astronatalai.backend.exception.EngineUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

@Component
public class AstroEngineClient {

    private final RestClient restClient;

    public AstroEngineClient(RestClient.Builder builder,
                             @Value("${app.astro-engine.url:http://localhost:3001}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public JsonNode natalChart(NatalChartCommand command) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fecha", command.fecha().toString());
        payload.put("hora", command.hora() == null ? null : command.hora().toString());
        payload.put("time_known", command.timeKnown());
        payload.put("lat", command.lat());
        payload.put("lng", command.lng());
        payload.put("timezone", command.timezone());
        payload.put("houseSystem", "whole-sign");
        return postForObject("/natal-chart", payload);
    }

    public JsonNode transits(JsonNode chart, LocalDate fecha) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("carta", chart);
        payload.put("fecha", fecha.toString());
        return postForObject("/transits", payload);
    }

    private JsonNode postForObject(String path, Map<String, Object> payload) {
        try {
            JsonNode response = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.isObject()) {
                throw new EngineUnavailableException("El motor astrológico devolvió una respuesta inválida");
            }
            return response;
        } catch (EngineUnavailableException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new EngineUnavailableException("El motor astrológico no está disponible", exception);
        }
    }
}
