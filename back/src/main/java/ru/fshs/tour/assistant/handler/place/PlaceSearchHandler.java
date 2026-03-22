package ru.fshs.tour.assistant.handler.place;

import java.util.List;
import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.conversation.interpreter.dto.RequestEntitiesDto;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.place.PlaceAssistantService;
import ru.fshs.tour.assistant.domain.place.PlaceSearchCriteria;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class PlaceSearchHandler extends AbstractAssistantHandler {

    private final PlaceAssistantService placeAssistantService;

    public PlaceSearchHandler(PlaceAssistantService placeAssistantService) {
        super(AssistantIntent.PLACE_SEARCH);
        this.placeAssistantService = placeAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        RequestEntitiesDto entities = context.getRequest().getEntities();
        var criteria = PlaceSearchCriteria.builder()
                .queryText(context.getRequest().getOriginalMessage())
                .locationText(entities != null ? entities.getLocationText() : null)
                .lat(entities != null ? entities.getLat() : null)
                .lng(entities != null ? entities.getLng() : null)
                .tags(entities != null && entities.getTags() != null ? entities.getTags() : List.of())
                .categories(entities != null && entities.getCategories() != null ? entities.getCategories() : List.of())
                .constraints(entities != null && entities.getConstraints() != null ? entities.getConstraints() : List.of())
                .build();
        return placeAssistantService.search(criteria);
    }
}
