package ru.fshs.tour.controller.dto.routes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AiRouteDraftDto(
        @NotBlank String summary,
        String advice,
        Integer totalDuration,
        String date,
        String imageUrl,
        Boolean canBeSaved,
        @NotEmpty List<@Valid AiRouteDraftStepDto> steps
) {
}
