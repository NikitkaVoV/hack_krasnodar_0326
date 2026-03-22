package ru.fshs.tour.assistant.orchestration;

import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;
import ru.fshs.tour.assistant.orchestration.context.AssistantContext;

/**
 * Coordinates routing and concrete handler invocation.
 */
public interface AssistantOrchestrator {

    AssistantOrchestrationResult orchestrate(NormalizedAssistantRequest request, AssistantContext assistantContext);
}