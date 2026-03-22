package ru.fshs.tour;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RouteStepsPublicIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldBePublicAndReturn404WhenRouteMissing() throws Exception {
        mockMvc.perform(get("/api/route-steps")
                        .param("routeId", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Route not found"));
    }

    @Test
    void shouldReturn422WhenRouteIdMissing() throws Exception {
        mockMvc.perform(get("/api/route-steps"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("routeId is required"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn422WhenRouteIdInvalid() throws Exception {
        mockMvc.perform(get("/api/route-steps")
                        .param("routeId", "not-a-uuid"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
