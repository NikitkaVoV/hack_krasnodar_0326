package ru.fshs.tour.assistant.conversation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.model.AssistantRole;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogMessage;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogMessageRepository;
import ru.fshs.tour.assistant.persistence.dialog.AiDialogSession;

@Service
@RequiredArgsConstructor
public class DialogMessageServiceImpl implements DialogMessageService {

    private final AiDialogMessageRepository aiDialogMessageRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AiDialogMessage saveUserMessage(AiDialogSession session, String messageText) {
        var message = new AiDialogMessage();
        message.setSession(session);
        message.setRole(AssistantRole.USER.name());
        message.setMessageText(messageText);
        return aiDialogMessageRepository.save(message);
    }

    @Override
    @Transactional
    public AiDialogMessage saveAssistantMessage(
            AiDialogSession session,
            String messageText,
            NormalizedAssistantRequest request,
            AssistantSystemResponse systemResponse
    ) {
        var message = new AiDialogMessage();
        message.setSession(session);
        message.setRole(AssistantRole.ASSISTANT.name());
        message.setMessageText(messageText);
        message.setIntent(request != null && request.getIntent() != null ? request.getIntent().name() : null);
        message.setEntitiesJson(writeJson(request != null ? request.getEntities() : null));
        message.setSystemResponseJson(writeJson(systemResponse));
        return aiDialogMessageRepository.save(message);
    }

    private JsonNode writeJson(Object source) {
        if (source == null) {
            return null;
        }
        return objectMapper.valueToTree(source);
    }
}
