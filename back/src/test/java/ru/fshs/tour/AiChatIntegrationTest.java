package ru.fshs.tour;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.domain.route.RouteStep;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteStepRepository routeStepRepository;

    @BeforeEach
    void setUp() {
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        routeStepRepository.deleteAll();
        routeRepository.deleteAll();
        eventRepository.deleteAll();
        placeRepository.deleteAll();
    }

    @Test
    void shouldBuildRouteFromExistingEntitiesOnly() throws Exception {
        var botanicalGarden = placeRepository.save(buildPlace(
                "Botanical Garden",
                "Nature park with green areas and animals.",
                "Krasnodar central district",
                45.0355,
                38.9753,
                90
        ));
        var safariPark = placeRepository.save(buildPlace(
                "Safari Park",
                "Family place with animals.",
                "Krasnodar zoo line",
                45.0460,
                38.9910,
                80
        ));
        var openAirEvent = eventRepository.save(buildEvent(
                "Open Air Ecology Talk",
                "Educational event about nature and wildlife.",
                "Krasnodar central district",
                45.0400,
                38.9800,
                LocalDateTime.of(2026, 6, 10, 12, 0),
                LocalDateTime.of(2026, 6, 10, 15, 0),
                60
        ));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Build route in Krasnodar on 2026-06-10 for 300 minutes with nature and animals"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").isNotEmpty())
                .andExpect(jsonPath("$.route.steps", not(empty())))
                .andExpect(jsonPath("$.route.totalDuration", greaterThan(0)))
                .andExpect(jsonPath("$.route.totalDuration", greaterThanOrEqualTo(45)))
                .andExpect(jsonPath("$.route.steps[0].durationMinutes", greaterThanOrEqualTo(45)))
                .andExpect(jsonPath("$.route.steps[0].id", anyOf(
                        is(botanicalGarden.getId().toString()),
                        is(safariPark.getId().toString()),
                        is(openAirEvent.getId().toString())
                )));
    }

    @Test
    void shouldExplainUnavailableActivityAndSuggestAlternatives() throws Exception {
        placeRepository.save(buildPlace(
                "Horse Riding Club",
                "Safe horse riding for visitors.",
                "Krasnodar horse district",
                45.0500,
                38.9700,
                70
        ));
        placeRepository.save(buildPlace(
                "City Zoo",
                "Animals and family-friendly attractions.",
                "Krasnodar zoo district",
                45.0480,
                38.9730,
                80
        ));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "I want bear riding in Krasnodar for 120 minutes"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", containsString("This activity is not available")))
                .andExpect(jsonPath("$.route.suggestions", not(empty())));
    }

    @Test
    void shouldFallbackToSavedRouteWhenNoResultsFound() throws Exception {
        var fallbackPlace = placeRepository.save(buildPlace(
                "Historic Museum",
                "Indoor museum route point.",
                "Krasnodar old town",
                45.0450,
                38.9780,
                60
        ));

        var fallbackRoute = new Route();
        fallbackRoute.setDate(LocalDate.of(2026, 6, 10));
        fallbackRoute.setTotalDuration(120);
        fallbackRoute.setSummary("Popular fallback route");
        fallbackRoute.setAdvice("Best for first-time visitors.");
        fallbackRoute = routeRepository.save(fallbackRoute);

        var step = new RouteStep();
        step.setRoute(fallbackRoute);
        step.setTargetId(fallbackPlace.getId());
        step.setTargetType("place");
        step.setStepOrder(1);
        routeStepRepository.save(step);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Build route in Atlantis on 2026-06-10 for 120 minutes"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.fallbackUsed", is(true)))
                .andExpect(jsonPath("$.route.steps", not(empty())))
                .andExpect(jsonPath("$.route.steps[0].id", is(fallbackPlace.getId().toString())));
    }

    private Place buildPlace(
            String name,
            String description,
            String address,
            double lat,
            double lng,
            int recommendedDuration
    ) {
        var place = new Place();
        place.setName(name);
        place.setDescription(description);
        place.setAddress(address);
        place.setLat(BigDecimal.valueOf(lat));
        place.setLng(BigDecimal.valueOf(lng));
        place.setRecommendedDuration(recommendedDuration);
        place.setHas3d(false);
        place.setHasVr(false);
        place.setVerified(true);
        place.setStatus("ACTIVE");
        return place;
    }

    private Event buildEvent(
            String name,
            String description,
            String address,
            double lat,
            double lng,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int recommendedDuration
    ) {
        var event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setAddress(address);
        event.setLat(BigDecimal.valueOf(lat));
        event.setLng(BigDecimal.valueOf(lng));
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setRecommendedDuration(recommendedDuration);
        event.setStatus("ACTIVE");
        return event;
    }
}
