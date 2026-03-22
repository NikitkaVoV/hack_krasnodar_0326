package ru.fshs.tour.assistant.handler.fallback;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantResultFactory;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class UnsupportedHandler extends AbstractAssistantHandler {

    public UnsupportedHandler() {
        super(AssistantIntent.UNSUPPORTED);
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        return AssistantResultFactory.unsupported("Запрос пока не поддерживается текущей версией ассистента.");
    }
}