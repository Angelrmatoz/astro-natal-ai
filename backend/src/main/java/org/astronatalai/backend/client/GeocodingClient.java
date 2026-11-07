package org.astronatalai.backend.client;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.astronatalai.backend.dto.GeocodingResult;
import org.astronatalai.backend.exception.GeocodingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GeocodingClient {

    private final RestClient restClient;

    public GeocodingClient(RestClient.Builder builder,
                           @Value("${app.geocoding.url:https://geocoding-api.open-meteo.com}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public GeocodingResult geocode(String ciudad) {
        try {
            OpenMeteoResponse response = restClient.get()
                    .uri(uri -> uri.path("/v1/search")
                            .queryParam("name", ciudad)
                            .queryParam("count", 1)
                            .queryParam("language", "es")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);

            if (response == null || response.results() == null || response.results().isEmpty()) {
                throw new GeocodingException("No se encontró la ciudad: " + ciudad);
            }

            OpenMeteoResult result = response.results().getFirst();
            if (result.timezone() == null || result.timezone().isBlank()) {
                throw new GeocodingException("La ciudad no tiene una zona horaria IANA");
            }

            return new GeocodingResult(
                    displayName(result), result.latitude(), result.longitude(), result.timezone());
        } catch (GeocodingException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new GeocodingException("No se pudo consultar el servicio de geocoding", exception);
        }
    }

    private static String displayName(OpenMeteoResult result) {
        return Stream.of(result.name(), result.admin1(), result.country())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));
    }

    record OpenMeteoResponse(List<OpenMeteoResult> results) {
    }

    record OpenMeteoResult(
            String name,
            String country,
            String admin1,
            double latitude,
            double longitude,
            String timezone) {
    }
}
