package ru.fshs.tour.assistant.handler.route;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.route.RouteAssistantService;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class RouteExplainHandler extends AbstractAssistantHandler {

    private final RouteAssistantService routeAssistantService;

    public RouteExplainHandler(RouteAssistantService routeAssistantService) {
        super(AssistantIntent.ROUTE_EXPLAIN);
        this.routeAssistantService = routeAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        var refs = context.getRequest().getReferences();
        return routeAssistantService.explainRoute(refs != null ? refs.getRouteId() : null);
    }
}