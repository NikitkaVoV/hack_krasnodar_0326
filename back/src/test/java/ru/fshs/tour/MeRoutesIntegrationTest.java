package ru.fshs.tour;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.domain.user.User;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MeRoutesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User anotherUser;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        routeRepository.deleteAll();
        adminUser = userRepository.findByLogin("admin").orElseThrow();
        anotherUser = userRepository.findByLogin("other_user")
                .orElseGet(() -> userRepository.save(User.builder()
                        .login("other_user")
                        .passwordHash("test_hash")
                        .name("Other User")
                        .userType("USER")
                        .build()));
        adminToken = loginAndGetAccessToken("admin", "admin");
    }

    @Test
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/me/routes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnOnlyCurrentUsersRoutes() throws Exception {
        saveRoute(adminUser, LocalDate.of(2026, 3, 21), "admin route");
        saveRoute(anotherUser, LocalDate.of(2026, 3, 20), "foreign route");

        mockMvc.perform(get("/api/me/routes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].summary").value("admin route"))
                .andExpect(jsonPath("$.items[0].user").value(adminUser.getId().toString()));
    }

    @Test
    void shouldDeleteOwnRoute() throws Exception {
        var route = saveRoute(adminUser, LocalDate.of(2026, 3, 22), "to delete");

        mockMvc.perform(delete("/api/me/routes/{id}", route.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn403ForForeignRouteDelete() throws Exception {
        var route = saveRoute(anotherUser, LocalDate.of(2026, 3, 22), "foreign");

        mockMvc.perform(delete("/api/me/routes/{id}", route.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ROUTE_FORBIDDEN"));
    }

    @Test
    void shouldReturn404ForMissingRouteDelete() throws Exception {
        mockMvc.perform(delete("/api/me/routes/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROUTE_NOT_FOUND"));
    }

    private Route saveRoute(User user, LocalDate date, String summary) {
        var route = new Route();
        route.setUser(user);
        route.setDate(date);
        route.setTotalDuration(120);
        route.setSummary(summary);
        route.setAdvice("advice");
        return routeRepository.save(route);
    }

    private String loginAndGetAccessToken(String login, String password) throws Exception {
        var loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "password": "%s"
                                }
                                """.formatted(login, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
