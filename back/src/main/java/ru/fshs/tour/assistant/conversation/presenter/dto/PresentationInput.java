package ru.fshs.tour.assistant.conversation.presenter.dto;

import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;

public record PresentationInput(
        NormalizedAssistantRequest request,
        AssistantSystemResponse systemResponse
) {
}