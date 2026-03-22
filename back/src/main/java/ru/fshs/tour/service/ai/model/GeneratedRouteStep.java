package ru.fshs.tour.service.ai.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GeneratedRouteStep(
        String type,
        UUID id,
        String title,
        int durationMinutes,
        LocalDateTime plannedStart,
        LocalDateTime plannedEnd,
        BigDecimal lat,
        BigDecimal lng,
        String address
) {
}
