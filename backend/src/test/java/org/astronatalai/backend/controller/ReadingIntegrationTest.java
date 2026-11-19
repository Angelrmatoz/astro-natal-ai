package org.astronatalai.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.astronatalai.backend.AbstractIntegrationTest;
import org.astronatalai.backend.client.AstroEngineClient;
import org.astronatalai.backend.client.GemmaClient;
import org.astronatalai.backend.exception.AiUnavailableException;
import org.astronatalai.backend.exception.EngineUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class ReadingIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AstroEngineClient astroEngineClient;

    @MockitoBean
    private GemmaClient gemmaClient;

    @Test
    void generatesReadingOnceAndReturnsStoredVersion() throws Exception {
        String token = register(uniqueEmail());
        String chartId = createChart(token);
        stubReadingDependencies();

        MvcResult first = postReading(token, chartId, "day")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("day"))
                .andExpect(jsonPath("$.contenidoTexto").value("Lectura de prueba"))
                .andExpect(jsonPath("$.modelo").value("gemma-4-31b-it"))
                .andExpect(jsonPath("$.rangoInicio").isNotEmpty())
                .andReturn();

        String firstId = JsonPath.read(first.getResponse().getContentAsString(), "$.id");

        MvcResult second = postReading(token, chartId, "day")
                .andExpect(status().isOk())
                .andReturn();

        String secondId = JsonPath.read(second.getResponse().getContentAsString(), "$.id");
        assertThat(secondId).isEqualTo(firstId);
        verify(gemmaClient, times(1)).generate(anyString());
        verify(astroEngineClient, times(1)).transits(any(), any());
    }

    @Test
    void generatesNatalReadingWithoutTransitsAndReturnsStoredVersion() throws Exception {
        String token = register(uniqueEmail());
        String chartId = createChart(token);
        when(gemmaClient.generate(anyString()))
                .thenReturn(new GemmaClient.GeneratedText("Carta interpretada", "gemma-4-31b-it"));

        MvcResult first = postReading(token, chartId, "natal")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("natal"))
                .andExpect(jsonPath("$.contenidoTexto").value("Carta interpretada"))
                .andExpect(jsonPath("$.rangoInicio").value("1990-05-01"))
                .andReturn();

        String firstId = JsonPath.read(first.getResponse().getContentAsString(), "$.id");

        MvcResult second = postReading(token, chartId, "natal")
                .andExpect(status().isOk())
                .andReturn();

        assertThat(JsonPath.<String>read(second.getResponse().getContentAsString(), "$.id"))
                .isEqualTo(firstId);
        verify(gemmaClient, times(1)).generate(anyString());
        verify(astroEngineClient, times(0)).transits(any(), any());
    }

    @Test
    void createsDifferentReadingsForDifferentPeriods() throws Exception {
        String token = register(uniqueEmail());
        String chartId = createChart(token);
        stubReadingDependencies();

        MvcResult day = postReading(token, chartId, "day")
                .andExpect(status().isOk())
                .andReturn();
        MvcResult week = postReading(token, chartId, "week")
                .andExpect(status().isOk())
                .andReturn();

        assertThat(JsonPath.<String>read(day.getResponse().getContentAsString(), "$.id"))
                .isNotEqualTo(JsonPath.read(week.getResponse().getContentAsString(), "$.id"));
        verify(gemmaClient, times(2)).generate(anyString());
    }

    @Test
    void anotherUserCannotGenerateReadingForTheChart() throws Exception {
        String owner = register(uniqueEmail());
        String chartId = createChart(owner);
        String other = register(uniqueEmail());

        postReading(other, chartId, "day")
                .andExpect(status().isNotFound());

        verifyNoInteractions(gemmaClient);
    }

    @Test
    void rejectsInvalidType() throws Exception {
        String token = register(uniqueEmail());

        postReading(token, UUID.randomUUID().toString(), "year")
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404WhenChartDoesNotExist() throws Exception {
        String token = register(uniqueEmail());

        postReading(token, UUID.randomUUID().toString(), "day")
                .andExpect(status().isNotFound());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/readings")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"chartId":"%s","type":"day"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns503WhenAiIsUnavailable() throws Exception {
        String token = register(uniqueEmail());
        String chartId = createChart(token);
        when(astroEngineClient.transits(any(), any())).thenReturn(transitData());
        when(gemmaClient.generate(anyString()))
                .thenThrow(new AiUnavailableException("down"));

        postReading(token, chartId, "day")
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void returns503WhenEngineIsUnavailable() throws Exception {
        String token = register(uniqueEmail());
        String chartId = createChart(token);
        when(astroEngineClient.transits(any(), any()))
                .thenThrow(new EngineUnavailableException("down"));

        postReading(token, chartId, "day")
                .andExpect(status().isServiceUnavailable());
    }

    private String createChart(String token) throws Exception {
        when(astroEngineClient.natalChart(any())).thenReturn(chartData());

        MvcResult result = mockMvc.perform(post("/api/charts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fechaNac": "1990-05-01",
                                  "horaNac": "14:30",
                                  "timeKnown": true,
                                  "latitud": 18.47186,
                                  "longitud": -69.89232,
                                  "timezone": "America/Santo_Domingo"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private void stubReadingDependencies() {
        when(astroEngineClient.transits(any(), any())).thenReturn(transitData());
        when(gemmaClient.generate(anyString()))
                .thenReturn(new GemmaClient.GeneratedText(
                        "Lectura de prueba", "gemma-4-31b-it"));
    }

    private ResultActions postReading(String token, String chartId, String type) throws Exception {
        return mockMvc.perform(post("/api/readings")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content("""
                        {"chartId":"%s","type":"%s"}
                        """.formatted(chartId, type)));
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
