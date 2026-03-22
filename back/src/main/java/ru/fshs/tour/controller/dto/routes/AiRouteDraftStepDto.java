package ru.fshs.tour.controller.dto.routes;

import jakarta.validation.constraints.NotBlank;

public record AiRouteDraftStepDto(
        @NotBlank String id,
        @NotBlank String type,
        @NotBlank String title,
        String description,
        String address,
        Double lat,
        Double lng,
        Integer duration,
        Integer orderIndex,
        String imageUrl,
        String plannedTimeStart,
        String plannedTimeEnd
) {
}
