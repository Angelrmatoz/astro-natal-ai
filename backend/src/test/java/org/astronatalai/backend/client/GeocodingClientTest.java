package org.astronatalai.backend.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.astronatalai.backend.dto.GeocodingResult;
import org.astronatalai.backend.exception.GeocodingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeocodingClientTest {

    private MockRestServiceServer server;
    private GeocodingClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new GeocodingClient(builder, "https://geo.test");
    }

    @Test
    void returnsFirstGeocodingResult() {
        server.expect(requestTo(startsWith("https://geo.test/v1/search")))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "Santo Domingo",
                              "country": "República Dominicana",
                              "admin1": "Distrito de Santo Domingo",
                              "latitude": 18.47186,
                              "longitude": -69.89232,
                              "timezone": "America/Santo_Domingo"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        GeocodingResult result = client.geocode("Santo Domingo");

        assertThat(result.ciudad())
                .isEqualTo("Santo Domingo, Distrito de Santo Domingo, República Dominicana");
        assertThat(result.latitud()).isEqualTo(18.47186);
        assertThat(result.longitud()).isEqualTo(-69.89232);
        assertThat(result.timezone()).isEqualTo("America/Santo_Domingo");
        server.verify();
    }

    @Test
    void rejectsUnknownCity() {
        server.expect(requestTo(startsWith("https://geo.test/v1/search")))
                .andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.geocode("Ninguna"))
                .isInstanceOf(GeocodingException.class)
                .hasMessageContaining("No se encontró");
    }

    @Test
    void mapsHttpErrorsToGeocodingException() {
        server.expect(requestTo(startsWith("https://geo.test/v1/search")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.geocode("Santo Domingo"))
                .isInstanceOf(GeocodingException.class);
    }
}
