package ru.fshs.tour.controller.dto.routes;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Route item for routes list endpoint")
public record RouteListItemDto(
        String id,
        String user,
        String date,
        Integer totalDuration,
        String summary,
        String advice,
        String title,
        String description,
        String imageUrl,
        String coverImage,
        String previewImage,
        List<Object> photos,
        List<Object> images,
        BigDecimal distanceKm,
        Integer estimatedBudget,
        List<String> tags,
        List<String> badges,
        List<String> suitableFor,
        String category
) {
}
