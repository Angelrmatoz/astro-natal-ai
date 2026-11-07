package org.astronatalai.backend.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalTime;
import org.astronatalai.backend.dto.NatalChartCommand;
import org.astronatalai.backend.exception.EngineUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class AstroEngineClientTest {

    private MockRestServiceServer server;
    private AstroEngineClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new AstroEngineClient(builder, "https://engine.test");
    }

    @Test
    void sendsContractPayloadAndReturnsChartJson() {
        server.expect(requestTo(startsWith("https://engine.test/natal-chart")))
                .andExpect(method(POST))
                .andExpect(content().json("""
                        {
                          "fecha": "1990-05-01",
                          "hora": "14:30",
                          "time_known": true,
                          "lat": 18.4861,
                          "lng": -69.9312,
                          "timezone": "America/Santo_Domingo",
                          "houseSystem": "whole-sign"
                        }
                        """, false))
                .andRespond(withSuccess("""
                        {
                          "ascendant": "Aries",
                          "planets": [{"name": "Sun", "sign": "Taurus", "degree": 10.5}]
                        }
                        """, MediaType.APPLICATION_JSON));

        JsonNode chart = client.natalChart(new NatalChartCommand(
                LocalDate.of(1990, 5, 1),
                LocalTime.of(14, 30),
                true,
                18.4861,
                -69.9312,
                "America/Santo_Domingo"));

        assertThat(chart.get("ascendant").asString()).isEqualTo("Aries");
        assertThat(chart.get("planets").get(0).get("name").asString()).isEqualTo("Sun");
        server.verify();
    }

    @Test
    void sendsTransitContractPayloadAndReturnsAspects() {
        server.expect(requestTo(startsWith("https://engine.test/transits")))
                .andExpect(method(POST))
                .andExpect(content().json("""
                        {
                          "carta": {"planets": [{"name": "Sun", "sign": "Taurus"}]},
                          "fecha": "2026-11-06"
                        }
                        """, false))
                .andRespond(withSuccess("""
                        {
                          "planets": [{"name": "Mars", "sign": "Leo"}],
                          "aspects": [{"transit": "Mars", "natal": "Sun", "type": "square", "orb": 1.2}]
                        }
                        """, MediaType.APPLICATION_JSON));

        JsonNode chart = JsonMapper.builder().build().readTree("""
                {"planets": [{"name": "Sun", "sign": "Taurus"}]}
                """);

        JsonNode transits = client.transits(chart, LocalDate.of(2026, 11, 6));

        assertThat(transits.get("planets").get(0).get("name").asString()).isEqualTo("Mars");
        assertThat(transits.get("aspects").get(0).get("type").asString()).isEqualTo("square");
        server.verify();
    }

    @Test
    void mapsServerErrorsToEngineUnavailable() {
        server.expect(requestTo(startsWith("https://engine.test/natal-chart")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.natalChart(command()))
                .isInstanceOf(EngineUnavailableException.class);
    }

    @Test
    void mapsTimeoutsToEngineUnavailable() {
        server.expect(requestTo(startsWith("https://engine.test/natal-chart")))
                .andRespond(withException(new SocketTimeoutException("timeout")));

        assertThatThrownBy(() -> client.natalChart(command()))
                .isInstanceOf(EngineUnavailableException.class);
    }

    private static NatalChartCommand command() {
        return new NatalChartCommand(
                LocalDate.of(1990, 5, 1),
                LocalTime.of(14, 30),
                true,
                18.4861,
                -69.9312,
                "America/Santo_Domingo");
    }
}
