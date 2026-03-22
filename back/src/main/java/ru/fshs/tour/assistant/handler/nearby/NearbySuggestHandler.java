package ru.fshs.tour.assistant.handler.nearby;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.place.PlaceAssistantService;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class NearbySuggestHandler extends AbstractAssistantHandler {

    private final PlaceAssistantService placeAssistantService;

    public NearbySuggestHandler(PlaceAssistantService placeAssistantService) {
        super(AssistantIntent.NEARBY_SUGGEST);
        this.placeAssistantService = placeAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        var entities = context.getRequest().getEntities();
        var lat = entities != null ? entities.getLat() : null;
        var lng = entities != null ? entities.getLng() : null;

        if (lat == null && context.getAssistantContext() != null
                && context.getAssistantContext().getUserProfile() != null) {
            lat = context.getAssistantContext().getUserProfile().getLastLat();
        }
        if (lng == null && context.getAssistantContext() != null
                && context.getAssistantContext().getUserProfile() != null) {
            lng = context.getAssistantContext().getUserProfile().getLastLng();
        }

        return placeAssistantService.nearbySuggestions(lat, lng, 5.0d);
    }
}
