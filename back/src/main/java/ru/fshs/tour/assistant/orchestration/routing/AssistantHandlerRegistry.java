package ru.fshs.tour.assistant.orchestration.routing;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.exception.UnsupportedAssistantIntentException;
import ru.fshs.tour.assistant.handler.AssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;

/**
 * Resolves concrete handler by intent.
 */
@Component
@RequiredArgsConstructor
public class AssistantHandlerRegistry {

    private final List<AssistantHandler> handlers;

    public AssistantHandler resolve(AssistantIntent intent) {
        return handlers.stream()
                .filter(handler -> handler.supports(intent))
                .findFirst()
                .orElseThrow(() -> new UnsupportedAssistantIntentException(
                        "No assistant handler for intent: " + intent
                ));
    }
}