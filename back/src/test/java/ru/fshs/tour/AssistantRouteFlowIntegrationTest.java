package ru.fshs.tour;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.domain.user.User;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;
import ru.fshs.tour.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssistantRouteFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteStepRepository routeStepRepository;

    private String adminToken;
    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        cleanup();
        adminUser = userRepository.findByLogin("admin").orElseThrow();
        adminUser.setLastLocationLat(BigDecimal.valueOf(45.0355));
        adminUser.setLastLocationLng(BigDecimal.valueOf(38.9753));
        userRepository.save(adminUser);
        adminToken = loginAndGetAccessToken("admin", "admin");
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldBuildRealRouteInAssistantWithoutStub() throws Exception {
        var first = placeRepository.save(buildPlace("Винодельня Южная", "Винодельня и дегустации", "Краснодар, ул. 1", 45.0400, 38.9700, 90));
        var second = placeRepository.save(buildPlace("Винодельня Терруар", "Экскурсии по производству вина", "Краснодар, ул. 2", 45.0450, 38.9780, 90));
        var third = placeRepository.save(buildPlace("Винодельня Солнечная", "Семейная винодельня", "Краснодар, ул. 3", 45.0500, 38.9850, 80));

        var response = mockMvc.perform(post("/api/assistant/messages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Подбери мне маршрут из 3 виноделен недалеко от меня"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply.text", containsString("маршрут")))
                .andExpect(jsonPath("$.reply.text", not(containsString("skeleton"))))
                .andExpect(jsonPath("$.systemResponse.payload.route.canBeSaved", is(true)))
                .andExpect(jsonPath("$.systemResponse.payload.route.steps", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();

        JsonNode root = objectMapper.readTree(response.getResponse().getContentAsString());
        JsonNode steps = root.at("/systemResponse/payload/route/steps");
        Set<String> allowedIds = Set.of(first.getId().toString(), second.getId().toString(), third.getId().toString());
        Set<String> returnedIds = new HashSet<>();
        steps.forEach(step -> returnedIds.add(step.path("id").asText()));

        org.assertj.core.api.Assertions.assertThat(returnedIds).isSubsetOf(allowedIds);
    }

    @Test
    void shouldReturnFewerPointsIfRequestedCountNotAvailable() throws Exception {
        var first = placeRepository.save(buildPlace("Винодельня А", "Локальная винодельня", "Краснодар, ул. А", 45.0400, 38.9700, 90));
        var second = placeRepository.save(buildPlace("Винодельня Б", "Локальная винодельня", "Краснодар, ул. Б", 45.0500, 38.9800, 90));

        var response = mockMvc.perform(post("/api/assistant/messages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Подбери мне маршрут из 3 виноделен рядом"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalizedRequest.intent", is("ROUTE_BUILD")))
                .andExpect(jsonPath("$.systemResponse.payload.route.steps", hasSize(2)))
                .andExpect(jsonPath("$.systemResponse.warnings[0]", containsString("Найдено")))
                .andReturn();

        JsonNode root = objectMapper.readTree(response.getResponse().getContentAsString());
        Set<String> returnedIds = new HashSet<>();
        root.at("/systemResponse/payload/route/steps").forEach(step -> returnedIds.add(step.path("id").asText()));
        org.assertj.core.api.Assertions.assertThat(returnedIds)
                .containsExactlyInAnyOrder(first.getId().toString(), second.getId().toString());
    }

    @Test
    void shouldSaveAssistantRouteAndShowItInProfile() throws Exception {
        placeRepository.save(buildPlace("Винодельня Север", "Вино и экскурсии", "Краснодар, ул. Север", 45.0370, 38.9710, 80));
        placeRepository.save(buildPlace("Винодельня Юг", "Вино и дегустации", "Краснодар, ул. Юг", 45.0430, 38.9790, 85));
        placeRepository.save(buildPlace("Винодельня Восток", "Вино и виды", "Краснодар, ул. Восток", 45.0490, 38.9860, 90));

        var chatResponse = mockMvc.perform(post("/api/assistant/messages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Подбери маршрут из 3 виноделен недалеко от меня"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemResponse.payload.route.canBeSaved", is(true)))
                .andReturn();

        JsonNode chatJson = objectMapper.readTree(chatResponse.getResponse().getContentAsString());
        JsonNode routeNode = chatJson.at("/systemResponse/payload/route");
        String savePayload = objectMapper.writeValueAsString(Map.of("route", routeNode));

        var saveResponse = mockMvc.perform(post("/api/routes/save-from-ai")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(savePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Маршрут сохранён")))
                .andExpect(jsonPath("$.routeId").isNotEmpty())
                .andReturn();

        String savedRouteId = objectMapper.readTree(saveResponse.getResponse().getContentAsString()).get("routeId").asText();

        mockMvc.perform(get("/api/me/routes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThan(0)))
                .andExpect(jsonPath("$.items[0].id").value(savedRouteId))
                .andExpect(jsonPath("$.items[0].summary", containsString("Маршрут")));

        mockMvc.perform(get("/api/route-steps")
                        .param("routeId", savedRouteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldRequireAuthForSavingRoute() throws Exception {
        mockMvc.perform(post("/api/routes/save-from-ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "route": {
                                    "summary": "test",
                                    "steps": [
                                      {
                                        "id": "00000000-0000-0000-0000-000000000000",
                                        "type": "place",
                                        "title": "x"
                                      }
                                    ]
                                  }
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private Place buildPlace(
            String name,
            String description,
            String address,
            double lat,
            double lng,
            int duration
    ) {
        var place = new Place();
        place.setName(name);
        place.setDescription(description);
        place.setAddress(address);
        place.setLat(BigDecimal.valueOf(lat));
        place.setLng(BigDecimal.valueOf(lng));
        place.setRecommendedDuration(duration);
        place.setHas3d(false);
        place.setHasVr(false);
        place.setVerified(true);
        place.setStatus("ACTIVE");
        return place;
    }

    private void cleanup() {
        routeStepRepository.deleteAll();
        routeRepository.deleteAll();
        eventRepository.deleteAll();
        placeRepository.deleteAll();
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
