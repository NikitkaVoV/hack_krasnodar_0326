package ru.fshs.tour.assistant.ai.prompt;

import org.springframework.stereotype.Component;

/**
 * Centralized source for reusable system prompts.
 */
@Component
public class SystemPromptFactory {

    public String interpretationSystemPrompt() {
        return "Interpret user request to normalized assistant JSON.";
    }

    public String presentationSystemPrompt() {
        return "Render concise answer for tourism assistant user.";
    }
}