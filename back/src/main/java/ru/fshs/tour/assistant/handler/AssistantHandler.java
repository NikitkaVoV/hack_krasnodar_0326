package ru.fshs.tour.assistant.handler;

import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

/**
 * Contract for intent-specific assistant handlers.
 */
public interface AssistantHandler {

    boolean supports(AssistantIntent intent);

    AssistantSystemResponse handle(AssistantHandlingContext context);
}