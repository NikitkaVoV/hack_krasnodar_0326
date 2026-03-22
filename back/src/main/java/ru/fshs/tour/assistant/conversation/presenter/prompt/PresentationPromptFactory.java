package ru.fshs.tour.assistant.conversation.presenter.prompt;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.conversation.presenter.dto.PresentationInput;

/**
 * Prompt factory extension point for future AI-based rendering.
 */
@Component
public class PresentationPromptFactory {

    public String buildSystemPrompt(PresentationInput input) {
        return """
                You are a travel assistant presenter.
                Convert structured system response into concise natural user-facing text.
                Keep factual meaning, do not fabricate data.
                """;
    }

    public String buildUserPrompt(PresentationInput input) {
        var requestIntent = input.request() != null && input.request().getIntent() != null
                ? input.request().getIntent().name()
                : "UNKNOWN";
        var status = input.systemResponse() != null && input.systemResponse().getStatus() != null
                ? input.systemResponse().getStatus().name()
                : "UNKNOWN";
        var summary = input.systemResponse() != null ? input.systemResponse().getSummary() : null;

        return """
                intent: %s
                status: %s
                summary: %s
                """.formatted(requestIntent, status, summary);
    }
}
