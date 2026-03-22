package ru.fshs.tour.assistant.domain.route;

import java.util.UUID;

public record RouteBuildCommand(
        UUID userId,
        String sourceMessage
) {
}