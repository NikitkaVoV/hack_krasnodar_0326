package ru.fshs.tour.controller.dto.route_steps;

import java.math.BigDecimal;
import java.util.List;

public record RouteStepPlaceDto(
        String id,
        String name,
        String title,
        String description,
        String location,
        String imageUrl,
        String coverImage,
        String previewImage,
        List<Object> photos,
        List<Object> images,
        String address,
        BigDecimal lat,
        BigDecimal lng,
        String category,
        Boolean isOpenNow
) {
}