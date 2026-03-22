package ru.fshs.tour.assistant.handler;

import lombok.RequiredArgsConstructor;
import ru.fshs.tour.assistant.domain.common.AssistantResponseType;
import ru.fshs.tour.assistant.domain.common.AssistantResultFactory;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.model.AssistantIntent;

/**
 * Base class with common helper methods for handlers.
 */
@RequiredArgsConstructor
public abstract class AbstractAssistantHandler implements AssistantHandler {

    private final AssistantIntent supportedIntent;

    @Override
    public boolean supports(AssistantIntent intent) {
        return supportedIntent == intent;
    }

    protected AssistantSystemResponse notImplemented(AssistantResponseType type, String summary) {
        return AssistantResultFactory.notImplemented(type, summary);
    }
}