package ru.fshs.tour.assistant.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AssistantMessageRequest(
        UUID sessionId,
        @NotBlank String message,
        ClientContextDto clientContext
) {
}
