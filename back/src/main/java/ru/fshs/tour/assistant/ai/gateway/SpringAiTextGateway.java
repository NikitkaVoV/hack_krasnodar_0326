package ru.fshs.tour.assistant.ai.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.ai.dto.AiTextRequest;
import ru.fshs.tour.assistant.exception.AssistantProcessingException;

/**
 * Spring AI-backed text gateway.
 */
@Component
@RequiredArgsConstructor
public class SpringAiTextGateway implements AiTextGateway {

    @Qualifier("assistantChatClient")
    private final ChatClient chatClient;

    @Override
    public String generateText(AiTextRequest request) {
        try {
            return chatClient.prompt()
                    .system(request.getSystemPrompt())
                    .user(request.getUserPrompt())
                    .call()
                    .content();
        } catch (Exception exception) {
            throw new AssistantProcessingException("Failed to generate text response via AI", exception);
        }
    }
}
