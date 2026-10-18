package com.swetonyancelmo.event_registration.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EventFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    // Admin criado pelo AdminSeeder com os valores do profile dev
    private static final String ADMIN_EMAIL = "admin@local.dev";
    private static final String ADMIN_PASSWORD = "admin12345";

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    void fullEventLifecycle() throws Exception {
        String location = mockMvc.perform(post("/api/v1/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/v1/events/")))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
        String eventPath = location.substring(location.indexOf("/api/v1/events/"));

        // rascunho não é visível para anônimos
        mockMvc.perform(get(eventPath)).andExpect(status().isNotFound());

        mockMvc.perform(post(eventPath + "/finish").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid state transition"));

        mockMvc.perform(post(eventPath + "/publish").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get(eventPath)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/events").param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").isNumber());

        mockMvc.perform(post(eventPath + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Speaker unavailable\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Speaker unavailable"));

        mockMvc.perform(post(eventPath + "/publish").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidSortPropertyReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/events").param("sort", "[\"string\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid parameter"));

        mockMvc.perform(get("/api/v1/events").param("sort", "organizer.password,asc"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/events").param("sort", "title,desc"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotCreateEvent() throws Exception {
        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON).content(eventJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotCreateEvent() throws Exception {
        String email = register();
        String userToken = login(email, "password123");

        mockMvc.perform(post("/api/v1/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicAdminEndpointNoLongerExists() throws Exception {
        mockMvc.perform(post("/api/v1/users/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"email\":\"x@x.com\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String email = register();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(email.toUpperCase())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already registered"));
    }

    @Test
    void invalidEventPayloadReturnsValidationErrors() throws Exception {
        String start = OffsetDateTime.now().plusDays(5).toString();
        String end = OffsetDateTime.now().plusDays(4).toString();
        String body = """
                {"title":"","description":"%s","startsAt":"%s","endsAt":"%s","location":"Online","capacity":0}
                """.formatted("x".repeat(256), start, end);

        mockMvc.perform(post("/api/v1/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.capacity").exists())
                .andExpect(jsonPath("$.errors.dateRangeValid").exists());
    }

    @Test
    void adminCanPromoteUser() throws Exception {
        String email = register();
        String userToken = login(email, "password123");
        String userId = JsonPath.read(mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/api/v1/users/" + userId + "/role")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/users/" + userId + "/role")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiDocsAndHealthArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    private String register() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@test.com";
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(email)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION));
        return email;
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    private static String userJson(String email) {
        return "{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"password123\"}";
    }

    private static String eventJson() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(7);
        return """
                {"title":"Spring Meetup","description":"Talks","startsAt":"%s","endsAt":"%s","location":"Online","capacity":100}
                """.formatted(start, start.plusHours(3));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

}
