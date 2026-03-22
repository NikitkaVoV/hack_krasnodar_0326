package ru.fshs.tour.assistant.ai.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.exception.AssistantProcessingException;

/**
 * Parses raw AI responses into typed objects.
 */
@Component
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public <T> T parseJson(String json, Class<T> responseType) {
        try {
            return objectMapper.readValue(json, responseType);
        } catch (JsonProcessingException exception) {
            throw new AssistantProcessingException("Failed to parse AI response JSON", exception);
        }
    }
}