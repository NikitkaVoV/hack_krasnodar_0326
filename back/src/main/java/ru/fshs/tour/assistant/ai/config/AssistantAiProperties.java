package ru.fshs.tour.assistant.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Feature toggles for assistant AI integration.
 */
@ConfigurationProperties(prefix = "assistant.ai")
@Getter
@Setter
public class AssistantAiProperties {

    private boolean classificationEnabled = true;
    private boolean presentationEnabled = true;
}
