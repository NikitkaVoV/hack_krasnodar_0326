package ru.fshs.tour.assistant.domain.route;

import java.util.UUID;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;

/**
 * Stub business facade for route-related assistant capabilities.
 */
public interface RouteAssistantService {

    AssistantSystemResponse buildRoute(RouteBuildCommand command);

    AssistantSystemResponse editRoute(RouteEditCommand command);

    AssistantSystemResponse explainRoute(UUID routeId);
}