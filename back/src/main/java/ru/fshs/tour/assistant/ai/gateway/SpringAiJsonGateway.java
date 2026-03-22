package ru.fshs.tour.assistant.ai.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.ai.dto.AiJsonRequest;
import ru.fshs.tour.assistant.exception.AssistantProcessingException;

/**
 * Spring AI-backed JSON gateway.
 */
@Component
@RequiredArgsConstructor
public class SpringAiJsonGateway implements AiJsonGateway {

    @Qualifier("assistantChatClient")
    private final ChatClient chatClient;

    @Override
    public <T> T generateJson(AiJsonRequest request, Class<T> responseType) {
        try {
            return chatClient.prompt()
                    .system(request.getSystemPrompt())
                    .user(request.getUserPrompt())
                    .call()
                    .entity(responseType);
        } catch (Exception exception) {
            throw new AssistantProcessingException("Failed to generate structured response via AI", exception);
        }
    }
}
