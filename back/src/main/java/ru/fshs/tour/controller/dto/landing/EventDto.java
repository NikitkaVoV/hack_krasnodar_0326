package ru.fshs.tour.controller.dto.landing;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Upcoming event contract for homepage")
public record EventDto(
        String id,
        String name,
        String description,
        String location,
        String startAt
) {
}
