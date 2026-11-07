package org.astronatalai.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.astronatalai.backend.AbstractIntegrationTest;
import org.astronatalai.backend.client.AstroEngineClient;
import org.astronatalai.backend.exception.EngineUnavailableException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class TransitIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AstroEngineClient astroEngineClient;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/transits/current"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns404WhenUserHasNoChart() throws Exception {
        String token = register(uniqueEmail());

        mockMvc.perform(get("/api/transits/current").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsCurrentTransitsForTheStoredChart() throws Exception {
        String token = register(uniqueEmail());
        createChart(token, true);
        when(astroEngineClient.transits(any(), any())).thenReturn(transitData());

        mockMvc.perform(get("/api/transits/current").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planets[0].name").value("Mars"))
                .andExpect(jsonPath("$.aspects[0].type").value("square"))
                .andExpect(jsonPath("$.aspects[0].natal").value("Sun"));
    }

    @Test
    void returns503WhenEngineIsUnavailable() throws Exception {
        String token = register(uniqueEmail());
        createChart(token, true);
        when(astroEngineClient.transits(any(), any()))
                .thenThrow(new EngineUnavailableException("down"));

        mockMvc.perform(get("/api/transits/current").header("Authorization", "Bearer " + token))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void passesChartWithoutTimeToTheEngine() throws Exception {
        String token = register(uniqueEmail());
        createChart(token, false);
        when(astroEngineClient.transits(any(), any())).thenReturn(transitData());

        mockMvc.perform(get("/api/transits/current").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        ArgumentCaptor<JsonNode> chart = ArgumentCaptor.forClass(JsonNode.class);
        verify(astroEngineClient).transits(chart.capture(), any());
        assertThat(chart.getValue().has("houses")).isFalse();
    }

    private void createChart(String token, boolean timeKnown) throws Exception {
        when(astroEngineClient.natalChart(any())).thenReturn(chartData());
        String hora = timeKnown ? "\"14:30\"" : "null";

        mockMvc.perform(post("/api/charts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fechaNac": "1990-05-01",
                                  "horaNac": %s,
                                  "timeKnown": %s,
                                  "latitud": 18.47186,
                                  "longitud": -69.89232,
                                  "timezone": "America/Santo_Domingo"
                                }
                                """.formatted(hora, timeKnown)))
                .andExpect(status().isOk());
    }

    private String register(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","nombre":"Ana"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private static String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }

    private static JsonNode chartData() {
        return JsonMapper.builder().build().readTree("""
                {
                  "ascendant": "Aries",
                  "planets": [{"name": "Sun", "sign": "Taurus", "degree": 10.5}]
                }
                """);
    }

    private static JsonNode transitData() {
        return JsonMapper.builder().build().readTree("""
                {
                  "planets": [{"name": "Mars", "sign": "Leo"}],
                  "aspects": [{"transit": "Mars", "natal": "Sun", "type": "square", "orb": 1.2}]
                }
                """);
    }
}
