package org.astronatalai.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.astronatalai.backend.AbstractIntegrationTest;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class AuthIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "secret12345678";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerReturnsTokenAndPersistsBcryptHash() throws Exception {
        String email = uniqueEmail();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerBody(email.toUpperCase(), PASSWORD, "Ana")))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("AUTH_TOKEN"))
                .andExpect(cookie().httpOnly("AUTH_TOKEN", true))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn();

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(JsonPath.<String>read(result.getResponse().getContentAsString(), "$.token"))
                .isNotBlank();
    }

    @Test
    void registerRejectsDuplicateEmailWithProblemDetail() throws Exception {
        String email = uniqueEmail();
        registerOk(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerBody(email, PASSWORD, "Ana")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {
        String email = uniqueEmail();
        registerOk(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginBody(email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        String email = uniqueEmail();
        registerOk(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginBody(email, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpointRequiresValidBearerToken() throws Exception {
        mockMvc.perform(get("/api/charts/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON));

        mockMvc.perform(get("/api/charts/me").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        String token = registerOk(uniqueEmail());

        mockMvc.perform(get("/api/charts/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void csrfEndpointIssuesTheCsrfCookie() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void csrfEndpointIgnoresAStaleSessionCookie() throws Exception {
        mockMvc.perform(get("/api/auth/csrf").cookie(new Cookie("AUTH_TOKEN", "invalid")))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void logoutRevokesTheSession() throws Exception {
        String token = registerOk(uniqueEmail());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/charts/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsTheAuthenticatedUser() throws Exception {
        String email = uniqueEmail();
        String token = registerOk(email);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void cookieAuthenticationRequiresCsrfForMutations() throws Exception {
        String token = registerOk(uniqueEmail());

        mockMvc.perform(post("/api/charts")
                        .cookie(new Cookie("AUTH_TOKEN", token))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"fechaNac":"1990-05-01","timeKnown":false,"ciudad":"Santo Domingo"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void blocksLoginAfterFiveFailures() throws Exception {
        String email = uniqueEmail();
        registerOk(email);

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(loginBody(email, "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginBody(email, PASSWORD)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void registerRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"no-es-email","password":"123","nombre":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    private String registerOk(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerBody(email, PASSWORD, "Ana")))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private static String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }

    private static String registerBody(String email, String password, String nombre) {
        return """
                {"email":"%s","password":"%s","nombre":"%s"}
                """.formatted(email, password, nombre);
    }

    private static String loginBody(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }
}
