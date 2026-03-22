package ru.fshs.tour.assistant.handler.place;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.place.PlaceAssistantService;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class PlaceExplainHandler extends AbstractAssistantHandler {

    private final PlaceAssistantService placeAssistantService;

    public PlaceExplainHandler(PlaceAssistantService placeAssistantService) {
        super(AssistantIntent.PLACE_EXPLAIN);
        this.placeAssistantService = placeAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        var references = context.getRequest().getReferences();
        return placeAssistantService.explain(references != null ? references.getPlaceId() : null);
    }
}