package ru.fshs.tour.assistant.api.dto;

import java.util.UUID;

public record ClientContextDto(
        UUID routeId,
        UUID placeId,
        UUID eventId
) {
}