package ru.fshs.tour.controller.dto.ai;

import java.math.BigDecimal;

public record AiRouteStepDto(
        String type,
        String id,
        String title,
        Integer durationMinutes,
        String plannedTimeStart,
        String plannedTimeEnd,
        BigDecimal lat,
        BigDecimal lng,
        String address
) {
}
