package ru.fshs.tour.controller.dto.map;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Map search response")
public record MapSearchResponseDto(
        List<MapSearchItemDto> items,
        int total
) {
}
