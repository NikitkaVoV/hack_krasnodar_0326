package ru.fshs.tour.assistant.handler.event;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.event.EventAssistantService;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class EventSearchHandler extends AbstractAssistantHandler {

    private final EventAssistantService eventAssistantService;

    public EventSearchHandler(EventAssistantService eventAssistantService) {
        super(AssistantIntent.EVENT_SEARCH);
        this.eventAssistantService = eventAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        var entities = context.getRequest().getEntities();
        return eventAssistantService.search(
                context.getRequest().getOriginalMessage(),
                entities != null ? entities.getLocationText() : null
        );
    }
}
