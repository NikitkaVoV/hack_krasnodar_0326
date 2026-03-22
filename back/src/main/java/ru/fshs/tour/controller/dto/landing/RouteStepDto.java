package ru.fshs.tour.controller.dto.landing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Route step contract for frontend")
public record RouteStepDto(
        String targetId,
        String targetType,
        Integer stepOrder,
        String plannedTimeStart,
        String plannedTimeEnd,
        Integer travelTimeMinutes,
        Integer waitTimeMinutes,
        String notes,
        String transportMode,
        Integer priority,
        BigDecimal lat,
        BigDecimal lng
) {
}
