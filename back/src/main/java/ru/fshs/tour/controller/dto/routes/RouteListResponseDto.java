package ru.fshs.tour.controller.dto.routes;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Routes list response")
public record RouteListResponseDto(
        List<RouteListItemDto> items,
        int total
) {
}
