package ru.fshs.tour.assistant.conversation;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.assistant.exception.AssistantProcessingException;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogSession;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogSessionRepository;

@Service
@RequiredArgsConstructor
public class DialogSessionServiceImpl implements DialogSessionService {

    private static final String DEFAULT_SESSION_STATUS = "ACTIVE";

    private final AiDialogSessionRepository aiDialogSessionRepository;

    @Override
    @Transactional
    public AiDialogSession getOrCreate(UUID sessionId, UUID userId) {
        if (sessionId != null) {
            var existing = aiDialogSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new AssistantProcessingException("Dialog session not found: " + sessionId));
            if (!existing.getUserId().equals(userId)) {
                throw new AssistantProcessingException("Dialog session does not belong to user: " + userId);
            }
            return existing;
        }

        var session = new AiDialogSession();
        session.setUserId(userId);
        session.setStatus(DEFAULT_SESSION_STATUS);
        session.setTitle("Assistant dialog");
        return aiDialogSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void updateLastIntent(AiDialogSession session, AssistantIntent intent) {
        session.setLastIntent(intent != null ? intent.name() : null);
        aiDialogSessionRepository.save(session);
    }
}