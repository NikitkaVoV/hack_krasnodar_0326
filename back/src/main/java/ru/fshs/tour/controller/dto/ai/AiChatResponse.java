package ru.fshs.tour.controller.dto.ai;

public record AiChatResponse(
        String text,
        AiRouteDto route
) {
}
