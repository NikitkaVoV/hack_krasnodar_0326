package ru.fshs.tour.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.controller.dto.landing.RouteStepDto;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.domain.route.RouteStep;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;

@Service
@Transactional(readOnly = true)
public class RouteStepService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final RouteStepRepository routeStepRepository;
    private final RouteRepository routeRepository;
    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;

    public RouteStepService(
            RouteStepRepository routeStepRepository,
            RouteRepository routeRepository,
            PlaceRepository placeRepository,
            EventRepository eventRepository
    ) {
        this.routeStepRepository = routeStepRepository;
        this.routeRepository = routeRepository;
        this.placeRepository = placeRepository;
        this.eventRepository = eventRepository;
    }

    public List<RouteStepDto> getStepsByRouteId(UUID routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found");
        }

        var steps = routeStepRepository.findAllByRouteIdOrderByStepOrderAsc(routeId);
        if (steps.isEmpty()) {
            return List.of();
        }

        var placeIds = steps.stream()
                .filter(step -> isPlaceType(step.getTargetType()))
                .map(RouteStep::getTargetId)
                .distinct()
                .toList();
        var eventIds = steps.stream()
                .filter(step -> isEventType(step.getTargetType()))
                .map(RouteStep::getTargetId)
                .distinct()
                .toList();

        Map<UUID, Place> places = placeRepository.findAllById(placeIds).stream()
                .collect(java.util.stream.Collectors.toMap(Place::getId, p -> p));
        Map<UUID, Event> events = eventRepository.findAllById(eventIds).stream()
                .collect(java.util.stream.Collectors.toMap(Event::getId, e -> e));

        return steps.stream()
                .map(step -> toDto(step, places.get(step.getTargetId()), events.get(step.getTargetId())))
                .toList();
    }

    private RouteStepDto toDto(RouteStep step, Place place, Event event) {
        var normalizedType = normalizeTargetType(step.getTargetType());
        var lat = place != null ? place.getLat() : null;
        var lng = place != null ? place.getLng() : null;

        if (event != null) {
            if (event.getLat() != null && event.getLng() != null) {
                lat = event.getLat();
                lng = event.getLng();
            } else if (event.getPlace() != null) {
                lat = event.getPlace().getLat();
                lng = event.getPlace().getLng();
            }
        }

        return new RouteStepDto(
                step.getTargetId() != null ? step.getTargetId().toString() : null,
                normalizedType,
                step.getStepOrder(),
                step.getPlannedTimeStart() != null ? step.getPlannedTimeStart().toLocalTime().format(HH_MM) : null,
                step.getPlannedTimeEnd() != null ? step.getPlannedTimeEnd().toLocalTime().format(HH_MM) : null,
                step.getTravelTimeMinutes() != null ? step.getTravelTimeMinutes() : 0,
                step.getWaitTimeMinutes() != null ? step.getWaitTimeMinutes() : 0,
                step.getNotes() != null ? step.getNotes() : "",
                step.getTransportMode() != null ? step.getTransportMode() : "",
                step.getPriority() != null ? step.getPriority() : 0,
                lat,
                lng
        );
    }

    private String normalizeTargetType(String rawType) {
        if (isPlaceType(rawType)) {
            return "place";
        }
        if (isEventType(rawType)) {
            return "event";
        }
        return rawType != null ? rawType.toLowerCase(Locale.ROOT) : "unknown";
    }

    private boolean isPlaceType(String rawType) {
        return rawType != null && rawType.toLowerCase(Locale.ROOT).contains("place");
    }

    private boolean isEventType(String rawType) {
        return rawType != null && rawType.toLowerCase(Locale.ROOT).contains("event");
    }
}
