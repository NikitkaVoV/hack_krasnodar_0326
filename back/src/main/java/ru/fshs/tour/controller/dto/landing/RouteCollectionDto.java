package ru.fshs.tour.controller.dto.landing;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Route collection metadata for landing page")
public record RouteCollectionDto(
        String id,
        String title,
        String description,
        String key
) {
}
