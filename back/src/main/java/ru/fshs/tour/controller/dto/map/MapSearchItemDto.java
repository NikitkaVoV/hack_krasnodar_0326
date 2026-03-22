package ru.fshs.tour.controller.dto.map;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Map search item")
public record MapSearchItemDto(
        String id,
        String type,
        String title,
        String description,
        BigDecimal lat,
        BigDecimal lng,
        BigDecimal distanceKm,
        String category,
        List<String> tags,
        String imageUrl,
        Integer popularity,
        boolean isOpenNow,
        boolean isFavorite,
        List<String> badges,
        String practicalInfo,
        Integer avgBudget,
        Integer avgDurationMinutes,
        String eventStartAt,
        String eventEndAt
) {
}
