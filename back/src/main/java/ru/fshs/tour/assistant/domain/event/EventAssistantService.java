package ru.fshs.tour.assistant.domain.event;

import java.util.UUID;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;

/**
 * Stub business facade for event-related assistant capabilities.
 */
public interface EventAssistantService {

    AssistantSystemResponse search(String queryText, String locationText);

    AssistantSystemResponse explain(UUID eventId);
}
