package ru.fshs.tour.mapper;

import org.springframework.stereotype.Component;
import ru.fshs.tour.controller.dto.landing.EventDto;
import ru.fshs.tour.controller.dto.landing.RouteDto;
import ru.fshs.tour.controller.dto.landing.RouteStepDto;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.domain.route.RouteStep;

@Component
public class LandingMapper {

    public RouteDto toRouteDto(Route route) {
        return new RouteDto(
                route.getId() != null ? route.getId().toString() : null,
                route.getUser() != null && route.getUser().getId() != null ? route.getUser().getId().toString() : null,
                route.getDate() != null ? route.getDate().toString() : null,
                route.getTotalDuration(),
                route.getSummary(),
                route.getAdvice()
        );
    }

    public EventDto toEventDto(Event event) {
        return new EventDto(
                event.getId() != null ? event.getId().toString() : null,
                event.getName(),
                event.getDescription(),
                event.getAddress(),
                event.getStartTime() != null ? event.getStartTime().toString() : null
        );
    }

    public RouteStepDto toRouteStepDto(RouteStep step) {
        return new RouteStepDto(
                step.getTargetId() != null ? step.getTargetId().toString() : null,
                step.getTargetType(),
                step.getStepOrder(),
                step.getPlannedTimeStart() != null ? step.getPlannedTimeStart().toString() : null,
                step.getPlannedTimeEnd() != null ? step.getPlannedTimeEnd().toString() : null,
                step.getTravelTimeMinutes(),
                step.getWaitTimeMinutes(),
                step.getNotes(),
                step.getTransportMode(),
                step.getPriority(),
                null,
                null
        );
    }
}
