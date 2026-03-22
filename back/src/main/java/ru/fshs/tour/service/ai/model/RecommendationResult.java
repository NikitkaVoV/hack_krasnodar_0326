package ru.fshs.tour.service.ai.model;

import java.util.List;

public record RecommendationResult(
        List<String> unavailableActivities,
        List<String> alternatives
) {
    public boolean hasUnavailableActivities() {
        return unavailableActivities != null && !unavailableActivities.isEmpty();
    }
}
