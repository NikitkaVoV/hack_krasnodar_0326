package ru.fshs.tour.assistant.handler.route;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.route.RouteAssistantService;
import ru.fshs.tour.assistant.domain.route.RouteBuildCommand;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class RouteBuildHandler extends AbstractAssistantHandler {

    private final RouteAssistantService routeAssistantService;

    public RouteBuildHandler(RouteAssistantService routeAssistantService) {
        super(AssistantIntent.ROUTE_BUILD);
        this.routeAssistantService = routeAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        var command = new RouteBuildCommand(
                context.getRequest().getUserId(),
                context.getRequest().getOriginalMessage()
        );
        return routeAssistantService.buildRoute(command);
    }
}