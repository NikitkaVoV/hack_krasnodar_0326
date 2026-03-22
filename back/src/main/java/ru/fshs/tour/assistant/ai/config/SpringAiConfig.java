package ru.fshs.tour.assistant.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Placeholder for Spring AI model client configuration.
 * TODO: add ChatClient bean wiring (OpenAI/Ollama/etc.) once provider is selected.
 */
@Configuration
@EnableConfigurationProperties(AssistantAiProperties.class)
public class SpringAiConfig {

    @Bean("assistantChatClient")
    ChatClient assistantChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
