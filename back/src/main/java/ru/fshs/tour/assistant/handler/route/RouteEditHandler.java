package ru.fshs.tour.assistant.handler.route;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.route.RouteAssistantService;
import ru.fshs.tour.assistant.domain.route.RouteEditCommand;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class RouteEditHandler extends AbstractAssistantHandler {

    private final RouteAssistantService routeAssistantService;

    public RouteEditHandler(RouteAssistantService routeAssistantService) {
        super(AssistantIntent.ROUTE_EDIT);
        this.routeAssistantService = routeAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        var refs = context.getRequest().getReferences();
        var command = new RouteEditCommand(
                refs != null ? refs.getRouteId() : null,
                context.getRequest().getOriginalMessage()
        );
        return routeAssistantService.editRoute(command);
    }
}