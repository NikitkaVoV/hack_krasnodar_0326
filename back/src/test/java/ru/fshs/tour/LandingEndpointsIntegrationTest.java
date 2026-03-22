package ru.fshs.tour;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LandingEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowPublicLandingEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/routes/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/routes/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/routes/recommended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/routes/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/events/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicRoutes").isArray())
                .andExpect(jsonPath("$.popularRoutes").isArray())
                .andExpect(jsonPath("$.recommendedRoutes").isArray())
                .andExpect(jsonPath("$.upcomingEvents").isArray())
                .andExpect(jsonPath("$.collections").isArray());
    }

    @Test
    void shouldValidateLandingRequestParams() throws Exception {
        mockMvc.perform(get("/api/routes/public").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/routes/public").param("date", "2026/03/21"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
