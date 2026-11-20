package org.astronatalai.backend.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.astronatalai.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class UserProfileIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "secret12345678";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void updatesTheProfileName() throws Exception {
        String token = register(uniqueEmail(), "Ana");

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana María"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana María"));
    }

    @Test
    void updatesTheTimezone() throws Exception {
        String token = register(uniqueEmail(), "Ana");

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","timezone":"Europe/Madrid"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value("Europe/Madrid"));
    }

    @Test
    void rejectsAnInvalidTimezone() throws Exception {
        String token = register(uniqueEmail(), "Ana");

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","timezone":"Marte/Olympus"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankName() throws Exception {
        String token = register(uniqueEmail(), "Ana");

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nombre":"   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String register(String email, String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","nombre":"%s"}
                                """.formatted(email, PASSWORD, nombre)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private static String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }
}
