package ru.fshs.tour.service.ai.model;

import java.util.List;

public record GeneratedRoute(
        String summary,
        int totalDuration,
        List<GeneratedRouteStep> steps,
        List<String> warnings,
        boolean fallbackUsed
) {
    public boolean isEmpty() {
        return steps == null || steps.isEmpty();
    }
}
