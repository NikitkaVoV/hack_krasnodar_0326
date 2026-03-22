package ru.fshs.tour.service.ai.model;

import java.time.LocalDate;
import java.util.List;

public record ParsedIntent(
        String intent,
        String originalMessage,
        String location,
        LocalDate date,
        int durationMinutes,
        List<String> preferences,
        List<String> constraints,
        String budget,
        List<String> requestedActivities
) {
}
