package org.astronatalai.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
import org.astronatalai.backend.client.GeocodingClient;
import org.astronatalai.backend.dto.GeocodingResult;
import org.astronatalai.backend.dto.NatalChartCommand;
import org.astronatalai.backend.exception.EngineUnavailableException;
import org.astronatalai.backend.exception.GeocodingException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class ChartIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "secret123";
    private static final String CITY = "Santo Domingo";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeocodingClient geocodingClient;

    @MockitoBean
    private AstroEngineClient astroEngineClient;

    @Test
    void createsChartWithGeocodedCityAndReturnsEngineData() throws Exception {
        String token = register(uniqueEmail());
        stubGeocoding();
        when(astroEngineClient.natalChart(any())).thenReturn(chartData());

        MvcResult result = mockMvc.perform(post("/api/charts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(cityBody(true, "14:30")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.ciudad").value(
                        "Santo Domingo, Distrito de Santo Domingo, República Dominicana"))
                .andExpect(jsonPath("$.timezone").value("America/Santo_Domingo"))
                .andExpect(jsonPath("$.datosCarta.planets[0].name").value("Sun"))
                .andReturn();

        String chartId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/charts/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chartId));
    }

    @Test
    void supportsUnknownBirthTime() throws Exception {
        String token = register(uniqueEmail());
        stubGeocoding();
        when(astroEngineClient.natalChart(any())).thenReturn(chartData());

        mockMvc.perform(post("/api/charts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(cityBody(false, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeKnown").value(false))
                .andExpect(jsonPath("$.horaNac").doesNotExist());

        ArgumentCaptor<NatalChartCommand> command = ArgumentCaptor.forClass(NatalChartCommand.class);
        verify(astroEngineClient).natalChart(command.capture());
        assertThat(command.getValue().timeKnown()).isFalse();
        assertThat(command.getValue().hora()).isNull();
    }

    @Test
    void savesManualCoordinatesWithoutGeocoding() throws Exception {
        String token = register(uniqueEmail());
        when(astroEngineClient.natalChart(any())).thenReturn(chartData());

        mockMvc.perform(post("/api/charts")
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
                .andExpect(jsonPath("$.ciudad").value("Coordenadas"))
                .andExpect(jsonPath("$.latitud").value(18.47186));

        verify(geocodingClient, never()).geocode(anyString());
    }

    @Test
    void secondSaveUpdatesTheSameChart() throws Exception {
        String token = register(uniqueEmail());
        stubGeocoding();
        when(astroEngineClient.natalChart(any())).thenReturn(chartData());

        String firstId = postChartAndReadId(token, cityBody(true, "14:30"));
        String secondId = postChartAndReadId(token, cityBody(true, "15:00"));

        assertThat(secondId).isEqualTo(firstId);
    }

    @Test
    void anotherUserCannotSeeTheChart() throws Exception {
        String owner = register(uniqueEmail());
        stubGeocoding();
        when(astroEngineClient.natalChart(any())).thenReturn(chartData());
        postChartAndReadId(owner, cityBody(true, "14:30"));

        String other = register(uniqueEmail());

        mockMvc.perform(get("/api/charts/me").header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns503WhenEngineIsUnavailable() throws Exception {
        String token = register(uniqueEmail());
        stubGeocoding();
        when(astroEngineClient.natalChart(any()))
                .thenThrow(new EngineUnavailableException("down"));

        mockMvc.perform(post("/api/charts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(cityBody(true, "14:30")))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void returns422WhenCityCannotBeGeocoded() throws Exception {
        String token = register(uniqueEmail());
        when(geocodingClient.geocode(CITY)).thenThrow(new GeocodingException("No se encontró la ciudad"));

        mockMvc.perform(post("/api/charts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(cityBody(true, "14:30")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejectsInconsistentBirthTime() throws Exception {
        String token = register(uniqueEmail());

        mockMvc.perform(post("/api/charts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(cityBody(true, null)))
                .andExpect(status().isBadRequest());
    }

    private void stubGeocoding() {
        when(geocodingClient.geocode(CITY)).thenReturn(new GeocodingResult(
                "Santo Domingo, Distrito de Santo Domingo, República Dominicana",
                18.47186,
                -69.89232,
                "America/Santo_Domingo"));
    }

    private String postChartAndReadId(String token, String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/charts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
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

    private static String cityBody(boolean timeKnown, String hora) {
        String horaJson = hora == null ? "null" : "\"" + hora + "\"";
        return """
                {
                  "nombre": "Carta de prueba",
                  "fechaNac": "1990-05-01",
                  "horaNac": %s,
                  "timeKnown": %s,
                  "ciudad": "%s"
                }
                """.formatted(horaJson, timeKnown, CITY);
    }

    private static JsonNode chartData() {
        return JsonMapper.builder().build().readTree("""
                {
                  "ascendant": "Aries",
                  "planets": [{"name": "Sun", "sign": "Taurus", "degree": 10.5}]
                }
                """);
    }
}
