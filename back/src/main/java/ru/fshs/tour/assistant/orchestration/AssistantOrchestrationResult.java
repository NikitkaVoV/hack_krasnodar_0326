package ru.fshs.tour.assistant.orchestration;

import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.orchestration.routing.ResolutionStage;

public record AssistantOrchestrationResult(
        AssistantSystemResponse systemResponse,
        ResolutionStage resolutionStage,
        String handlerName
) {
}