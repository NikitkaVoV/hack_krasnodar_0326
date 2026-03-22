package ru.fshs.tour;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogMessageRepository;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogSessionRepository;
import ru.fshs.tour.domain.user.User;
import ru.fshs.tour.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssistantConversationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiDialogSessionRepository aiDialogSessionRepository;

    @Autowired
    private AiDialogMessageRepository aiDialogMessageRepository;

    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        aiDialogMessageRepository.deleteAll();
        aiDialogSessionRepository.deleteAll();
        adminUser = userRepository.findByLogin("admin").orElseThrow();
        adminToken = loginAndGetAccessToken("admin", "admin");
    }

    @Test
    void shouldUseAuthenticatedUserAndPersistDialogMessages() throws Exception {
        var response = mockMvc.perform(post("/api/assistant/messages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Build route for weekend",
                                  "clientContext": {
                                    "routeId": null,
                                    "placeId": null,
                                    "eventId": null
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalizedRequest.intent").value("ROUTE_BUILD"))
                .andExpect(jsonPath("$.normalizedRequest.userId").value(adminUser.getId().toString()))
                .andExpect(jsonPath("$.debug.handlerName").value("RouteBuildHandler"))
                .andReturn();

        JsonNode json = objectMapper.readTree(response.getResponse().getContentAsString());
        UUID sessionId = UUID.fromString(json.get("sessionId").asText());

        assertThat(aiDialogSessionRepository.count()).isEqualTo(1);
        assertThat(aiDialogMessageRepository.count()).isEqualTo(2);

        var session = aiDialogSessionRepository.findById(sessionId).orElseThrow();
        assertThat(session.getUserId()).isEqualTo(adminUser.getId());

        Set<String> roles = aiDialogMessageRepository.findAll().stream()
                .map(message -> message.getRole())
                .collect(Collectors.toSet());
        assertThat(roles).containsExactlyInAnyOrder("USER", "ASSISTANT");
    }

    @Test
    void shouldRequireAuthenticationForAssistantEndpoint() throws Exception {
        mockMvc.perform(post("/api/assistant/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "РјР°СЂС€СЂСѓС‚"
                                }
                                """))
                .andExpect(status().isUnauthorized());
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