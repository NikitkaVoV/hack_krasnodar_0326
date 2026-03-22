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
class MapSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowPublicMapSearchWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/map/search")
                        .param("lat", "45.0355")
                        .param("lng", "38.9753")
                        .param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    void shouldReturnEmptyListWhenNoResults() throws Exception {
        mockMvc.perform(get("/api/map/search")
                        .param("lat", "0")
                        .param("lng", "0")
                        .param("radiusKm", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void shouldReturnBadRequestForInvalidParams() throws Exception {
        mockMvc.perform(get("/api/map/search")
                        .param("lat", "100")
                        .param("lng", "38.9753")
                        .param("radiusKm", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        mockMvc.perform(get("/api/map/search")
                        .param("lat", "45.0355")
                        .param("lng", "38.9753")
                        .param("radiusKm", "10")
                        .param("types", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
