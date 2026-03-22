package ru.fshs.tour.assistant.conversation.interpreter.dto;

import java.util.UUID;
import ru.fshs.tour.assistant.api.dto.ClientContextDto;
import ru.fshs.tour.assistant.orchestration.context.UserProfileSnapshot;

public record AssistantInterpretationInput(
        String message,
        UUID sessionId,
        UUID userId,
        ClientContextDto clientContext,
        UserProfileSnapshot userProfile,
        UUID activeRouteId,
        UUID currentPlaceId,
        UUID currentEventId
) {
}