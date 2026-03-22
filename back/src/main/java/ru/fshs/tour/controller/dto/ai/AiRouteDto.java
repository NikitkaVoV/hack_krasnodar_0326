package ru.fshs.tour.controller.dto.ai;

import java.util.List;

public record AiRouteDto(
        String summary,
        Integer totalDuration,
        List<AiRouteStepDto> steps,
        List<String> warnings,
        List<String> suggestions,
        boolean fallbackUsed
) {
}
