package ru.fshs.tour.assistant.orchestration.context;

import java.util.UUID;
import ru.fshs.tour.assistant.api.dto.ClientContextDto;

/**
 * Builds assistant context from user and client-provided metadata.
 */
public interface AssistantContextService {

    AssistantContext buildContext(UUID userId, ClientContextDto clientContext);
}