package ru.fshs.tour.controller.dto.landing;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public route contract for homepage")
public record RouteDto(
        String id,
        String user,
        String date,
        Integer totalDuration,
        String summary,
        String advice
) {
}
