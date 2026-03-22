package ru.fshs.tour.assistant.ai.gateway;

import ru.fshs.tour.assistant.ai.dto.AiTextRequest;

/**
 * Abstraction for plain text generation.
 */
public interface AiTextGateway {

    String generateText(AiTextRequest request);
}