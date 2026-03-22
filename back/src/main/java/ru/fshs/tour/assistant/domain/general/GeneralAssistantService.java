package ru.fshs.tour.assistant.domain.general;

import java.util.UUID;
import ru.fshs.tour.assistant.conversation.interpreter.dto.RequestEntitiesDto;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;

/**
 * Stub business facade for generic assistant scenarios.
 */
public interface GeneralAssistantService {

    AssistantSystemResponse answerQuestion(String message);

    AssistantSystemResponse updateProfile(UUID userId, String message, RequestEntitiesDto entities);
}
