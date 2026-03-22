package ru.fshs.tour.assistant.conversation;

import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogMessage;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogSession;

/**
 * Service for dialog message persistence.
 */
public interface DialogMessageService {

    AiDialogMessage saveUserMessage(AiDialogSession session, String messageText);

    AiDialogMessage saveAssistantMessage(
            AiDialogSession session,
            String messageText,
            NormalizedAssistantRequest request,
            AssistantSystemResponse systemResponse
    );
}