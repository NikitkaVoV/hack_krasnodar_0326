package ru.fshs.tour.assistant.orchestration.routing;

import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;

/**
 * Converts normalized request into routing stage metadata.
 */
public interface IntentRouter {

    ResolutionStage resolveStage(NormalizedAssistantRequest request);
}