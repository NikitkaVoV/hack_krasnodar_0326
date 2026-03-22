package ru.fshs.tour.assistant.api.dto;

import java.util.UUID;
import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;

public record AssistantMessageResponse(
        UUID sessionId,
        AssistantHumanReplyDto reply,
        NormalizedAssistantRequest normalizedRequest,
        AssistantSystemResponse systemResponse,
        AssistantDebugDto debug
) {
}