package ru.fshs.tour;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.domain.content.PlaceMedia;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.domain.route.RouteStep;
import ru.fshs.tour.domain.user.User;
import ru.fshs.tour.repository.PlaceMediaRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;
import ru.fshs.tour.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RouteImageCompatibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteStepRepository routeStepRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceMediaRepository placeMediaRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private String adminToken;
    private Route route;
    private Place place;

    @BeforeEach
    void setUp() throws Exception {
        routeStepRepository.deleteAll();
        routeRepository.deleteAll();
        placeMediaRepository.deleteAll();
        placeRepository.deleteAll();

        adminUser = userRepository.findByLogin("admin").orElseThrow();
        adminToken = loginAndGetAccessToken("admin", "admin");

        place = new Place();
        place.setName("Парк Галицкого");
        place.setDescription("Современный парк");
        place.setAddress("Краснодар");
        place.setLat(BigDecimal.valueOf(45.0448));
        place.setLng(BigDecimal.valueOf(39.0306));
        place.setStatus("ACTIVE");
        place.setHas3d(false);
        place.setHasVr(false);
        place.setVerified(true);
        place = placeRepository.save(place);

        var media = new PlaceMedia();
        media.setPlace(place);
        media.setMediaType("image");
        media.setUrl("https://cdn.example.com/places/place_1.jpg");
        media.setSortOrder(1);
        placeMediaRepository.save(media);

        route = new Route();
        route.setUser(adminUser);
        route.setDate(LocalDate.of(2026, 3, 21));
        route.setTotalDuration(240);
        route.setSummary("Прогулка по центру");
        route.setAdvice("Начните с парка");
        route = routeRepository.save(route);

        var step = new RouteStep();
        step.setRoute(route);
        step.setTargetId(place.getId());
        step.setTargetType("place");
        step.setStepOrder(1);
        routeStepRepository.save(step);
    }

    @Test
    void meRoutesShouldContainImageCompatibilityFields() throws Exception {
        mockMvc.perform(get("/api/me/routes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].imageUrl").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.items[0].coverImage").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.items[0].previewImage").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.items[0].photos").isArray())
                .andExpect(jsonPath("$.items[0].images").isArray());
    }

    @Test
    void routesListShouldContainSameImageCompatibilityFields() throws Exception {
        mockMvc.perform(get("/api/routes/list")
                        .param("userId", "public")
                        .param("date", "2026-03-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].imageUrl").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.items[0].coverImage").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.items[0].previewImage").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.items[0].photos").isArray())
                .andExpect(jsonPath("$.items[0].images").isArray());
    }

    @Test
    void placeTargetDetailsShouldExposeNameLocationAndImageAliases() throws Exception {
        mockMvc.perform(get("/api/route-steps/places/{id}", place.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(place.getId().toString()))
                .andExpect(jsonPath("$.name").value("Парк Галицкого"))
                .andExpect(jsonPath("$.title").value("Парк Галицкого"))
                .andExpect(jsonPath("$.location").value("Краснодар"))
                .andExpect(jsonPath("$.address").value("Краснодар"))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.coverImage").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.previewImage").value("https://cdn.example.com/places/place_1.jpg"))
                .andExpect(jsonPath("$.photos").isArray())
                .andExpect(jsonPath("$.images").isArray());
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
