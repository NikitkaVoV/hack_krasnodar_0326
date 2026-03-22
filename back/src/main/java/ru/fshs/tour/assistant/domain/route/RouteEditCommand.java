package ru.fshs.tour.assistant.domain.route;

import java.util.UUID;

public record RouteEditCommand(
        UUID routeId,
        String editInstruction
) {
}