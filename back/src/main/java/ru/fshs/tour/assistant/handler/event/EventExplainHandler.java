package ru.fshs.tour.assistant.handler.event;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.event.EventAssistantService;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class EventExplainHandler extends AbstractAssistantHandler {

    private final EventAssistantService eventAssistantService;

    public EventExplainHandler(EventAssistantService eventAssistantService) {
        super(AssistantIntent.EVENT_EXPLAIN);
        this.eventAssistantService = eventAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        var refs = context.getRequest().getReferences();
        return eventAssistantService.explain(refs != null ? refs.getEventId() : null);
    }
}