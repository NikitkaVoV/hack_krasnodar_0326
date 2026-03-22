package ru.fshs.tour.assistant.conversation;

import ru.fshs.tour.assistant.api.dto.AssistantMessageRequest;
import ru.fshs.tour.assistant.api.dto.AssistantMessageResponse;

/**
 * Main facade for assistant conversation lifecycle.
 */
public interface AssistantConversationService {

    AssistantMessageResponse processMessage(AssistantMessageRequest request);
}