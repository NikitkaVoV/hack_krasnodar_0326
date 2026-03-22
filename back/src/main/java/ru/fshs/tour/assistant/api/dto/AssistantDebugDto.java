package ru.fshs.tour.assistant.api.dto;

public record AssistantDebugDto(
        String detectedIntent,
        String resolutionStage,
        String handlerName
) {
}