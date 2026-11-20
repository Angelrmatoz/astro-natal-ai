package org.astronatalai.backend.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.astronatalai.backend.AbstractIntegrationTest;
import org.astronatalai.backend.client.GeocodingClient;
import org.astronatalai.backend.dto.GeocodingResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

class GeocodingControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeocodingClient geocodingClient;

    @Test
    void searchesCitiesWithoutAuthentication() throws Exception {
        when(geocodingClient.search(anyString())).thenReturn(List.of(
                new GeocodingResult(
                        "Santo Domingo, Distrito Nacional, República Dominicana",
                        18.47186,
                        -69.89232,
                        "America/Santo_Domingo")));

        mockMvc.perform(get("/api/geocoding/search").param("q", "Santo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ciudad").value(
                        "Santo Domingo, Distrito Nacional, República Dominicana"))
                .andExpect(jsonPath("$[0].latitud").value(18.47186))
                .andExpect(jsonPath("$[0].timezone").value("America/Santo_Domingo"));
    }

    @Test
    void ignoresQueriesShorterThanTwoCharacters() throws Exception {
        mockMvc.perform(get("/api/geocoding/search").param("q", "S"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
