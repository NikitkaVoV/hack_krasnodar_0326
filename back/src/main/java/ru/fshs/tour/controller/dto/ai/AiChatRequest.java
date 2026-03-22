package ru.fshs.tour.controller.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank String message
) {
}
