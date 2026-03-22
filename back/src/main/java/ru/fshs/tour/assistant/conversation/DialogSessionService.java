package ru.fshs.tour.assistant.conversation;

import java.util.UUID;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogSession;

/**
 * Service for dialog session lifecycle operations.
 */
public interface DialogSessionService {

    AiDialogSession getOrCreate(UUID sessionId, UUID userId);

    void updateLastIntent(AiDialogSession session, AssistantIntent intent);
}