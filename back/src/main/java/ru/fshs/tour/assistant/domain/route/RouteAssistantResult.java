package ru.fshs.tour.assistant.domain.route;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RouteAssistantResult(
        String note,
        UUID routeId,
        AssistantRoute route,
        List<String> suggestions
) {
    public RouteAssistantResult(String note, UUID routeId) {
        this(note, routeId, null, List.of());
    }

    public record AssistantRoute(
            UUID id,
            String summary,
            String advice,
            Integer totalDuration,
            LocalDate date,
            String imageUrl,
            List<AssistantRouteStep> steps,
            boolean canBeSaved
    ) {
        public AssistantRoute {
            steps = steps == null ? List.of() : List.copyOf(steps);
        }
    }

    public record AssistantRouteStep(
            String id,
            String type,
            String title,
            String description,
            String address,
            Double lat,
            Double lng,
            Integer duration,
            Integer orderIndex,
            String imageUrl
    ) {
    }
}
