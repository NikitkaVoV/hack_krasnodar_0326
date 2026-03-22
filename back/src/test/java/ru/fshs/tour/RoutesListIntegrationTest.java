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
class RoutesListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowPublicRoutesListForGuests() throws Exception {
        mockMvc.perform(get("/api/routes/list")
                        .param("userId", "public")
                        .param("date", "2026-03-21"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    void shouldReturnEmptyResultAs200() throws Exception {
        mockMvc.perform(get("/api/routes/list")
                        .param("userId", "public")
                        .param("date", "2100-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void shouldReturnBadRequestForInvalidParams() throws Exception {
        mockMvc.perform(get("/api/routes/list")
                        .param("userId", "")
                        .param("date", "2026-03-21"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/routes/list")
                        .param("userId", "not-a-uuid")
                        .param("date", "2026-03-21"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/routes/list")
                        .param("userId", "public")
                        .param("date", "2026/03/21"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowPublicAccessToRouteListMetaById() throws Exception {
        mockMvc.perform(get("/api/routes/list/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Route not found"));
    }
}
