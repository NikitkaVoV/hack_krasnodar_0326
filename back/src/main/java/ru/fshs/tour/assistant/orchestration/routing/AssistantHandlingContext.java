package ru.fshs.tour.assistant.orchestration.routing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;
import ru.fshs.tour.assistant.orchestration.context.AssistantContext;

@Getter
@AllArgsConstructor
public class AssistantHandlingContext {

    private final NormalizedAssistantRequest request;
    private final AssistantContext assistantContext;
}