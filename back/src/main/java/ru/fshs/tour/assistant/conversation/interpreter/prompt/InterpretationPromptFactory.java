package ru.fshs.tour.assistant.conversation.interpreter.prompt;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.conversation.interpreter.dto.AssistantInterpretationInput;

/**
 * Prompt factory extension point for future AI-based interpretation.
 */
@Component
public class InterpretationPromptFactory {

    public String buildSystemPrompt(AssistantInterpretationInput input) {
        return """
                You are a tourism assistant classifier.
                Return only structured fields for intent classification.
                Map user message to one of intents:
                PLACE_SEARCH, PLACE_EXPLAIN, EVENT_SEARCH, EVENT_EXPLAIN,
                ROUTE_BUILD, ROUTE_EDIT, ROUTE_EXPLAIN, NEARBY_SUGGEST,
                PROFILE_UPDATE, GENERAL_QUESTION, UNSUPPORTED.
                Fill entities/references only when explicit in user message.
                Do not invent IDs.
                """;
    }

    public String buildUserPrompt(AssistantInterpretationInput input) {
        return """
                userId: %s
                sessionId: %s
                message: %s
                clientContext.routeId: %s
                clientContext.placeId: %s
                clientContext.eventId: %s
                """.formatted(
                input.userId(),
                input.sessionId(),
                input.message(),
                input.clientContext() != null ? input.clientContext().routeId() : null,
                input.clientContext() != null ? input.clientContext().placeId() : null,
                input.clientContext() != null ? input.clientContext().eventId() : null
        );
    }
}
