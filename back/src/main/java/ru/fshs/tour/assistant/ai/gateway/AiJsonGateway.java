package ru.fshs.tour.assistant.ai.gateway;

import ru.fshs.tour.assistant.ai.dto.AiJsonRequest;

/**
 * Abstraction for structured JSON generation.
 */
public interface AiJsonGateway {

    <T> T generateJson(AiJsonRequest request, Class<T> responseType);
}