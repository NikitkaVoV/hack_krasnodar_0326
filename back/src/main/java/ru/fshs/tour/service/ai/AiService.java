package ru.fshs.tour.service.ai;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.controller.dto.ai.AiChatResponse;
import ru.fshs.tour.controller.dto.ai.AiRouteDto;
import ru.fshs.tour.controller.dto.ai.AiRouteStepDto;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.service.ai.model.GeneratedRoute;
import ru.fshs.tour.service.ai.model.ParsedIntent;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final int LOCATION_RADIUS_KM = 25;

    private final IntentParserService intentParserService;
    private final RouteBuilderService routeBuilderService;
    private final RecommendationService recommendationService;
    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;

    public AiChatResponse chat(String message) {
        var intent = intentParserService.parse(message);

        var places = placeRepository.findAll().stream()
                .filter(this::isAvailablePlace)
                .toList();
        var events = eventRepository.findAll().stream()
                .filter(this::isAvailableEvent)
                .toList();

        var locationFiltered = filterByLocation(intent.location(), places, events);
        var filteredPlaces = locationFiltered.places();
        var filteredEvents = filterEventsByDate(intent, locationFiltered.events());

        var recommendation = recommendationService.analyzeRequest(intent.requestedActivities(), places, events);

        var warnings = new ArrayList<>(locationFiltered.warnings());
        var builtRoute = routeBuilderService.buildRoute(intent, filteredPlaces, filteredEvents);
        warnings.addAll(builtRoute.warnings());

        GeneratedRoute finalRoute;
        if (builtRoute.isEmpty()) {
            var fallbackRoute = routeBuilderService.buildFallbackRoute(intent);
            warnings.addAll(fallbackRoute.warnings());
            finalRoute = new GeneratedRoute(
                    fallbackRoute.summary(),
                    fallbackRoute.totalDuration(),
                    fallbackRoute.steps(),
                    List.copyOf(new LinkedHashSet<>(warnings)),
                    fallbackRoute.fallbackUsed()
            );
        } else {
            finalRoute = new GeneratedRoute(
                    builtRoute.summary(),
                    builtRoute.totalDuration(),
                    builtRoute.steps(),
                    List.copyOf(new LinkedHashSet<>(warnings)),
                    builtRoute.fallbackUsed()
            );
        }

        var responseText = buildHumanResponse(intent, finalRoute, recommendation);
        return new AiChatResponse(responseText, toRouteDto(finalRoute, recommendation.alternatives()));
    }

    private AiRouteDto toRouteDto(GeneratedRoute route, List<String> suggestions) {
        var steps = route.steps().stream()
                .map(step -> new AiRouteStepDto(
                        step.type(),
                        step.id() != null ? step.id().toString() : null,
                        step.title(),
                        step.durationMinutes(),
                        step.plannedStart() != null ? step.plannedStart().toLocalTime().format(HH_MM) : null,
                        step.plannedEnd() != null ? step.plannedEnd().toLocalTime().format(HH_MM) : null,
                        step.lat(),
                        step.lng(),
                        step.address()
                ))
                .toList();

        return new AiRouteDto(
                route.summary(),
                route.totalDuration(),
                steps,
                route.warnings(),
                suggestions == null ? List.of() : suggestions,
                route.fallbackUsed()
        );
    }

    private String buildHumanResponse(
            ParsedIntent intent,
            GeneratedRoute route,
            ru.fshs.tour.service.ai.model.RecommendationResult recommendation
    ) {
        var fragments = new ArrayList<String>();

        if (recommendation.hasUnavailableActivities()) {
            fragments.add("This activity is not available: " + String.join(", ", recommendation.unavailableActivities()) + ".");
            if (!recommendation.alternatives().isEmpty()) {
                fragments.add("Closest alternatives from available data: " + String.join(", ", recommendation.alternatives()) + ".");
            }
        }

        if (route.steps().isEmpty()) {
            fragments.add("I could not build a route from current places/events for your request.");
            return String.join(" ", fragments);
        }

        var highlights = route.steps().stream()
                .limit(3)
                .map(step -> step.title())
                .toList();
        fragments.add("I built a route with " + route.steps().size() + " stops and total duration " + route.totalDuration() + " minutes.");
        fragments.add("Highlights: " + String.join(", ", highlights) + ".");
        if (route.fallbackUsed()) {
            fragments.add("I used an existing popular route as fallback because no direct match was available.");
        }
        if (intent.location() != null && !intent.location().isBlank()) {
            fragments.add("Location focus: " + intent.location() + ".");
        }
        if (intent.date() != null) {
            fragments.add("Planned date: " + intent.date() + ".");
        }
        return String.join(" ", fragments);
    }

    private FilteredData filterByLocation(String location, List<Place> places, List<Event> events) {
        if (location == null || location.isBlank()) {
            return new FilteredData(places, events, List.of());
        }

        var normalizedLocation = location.toLowerCase(Locale.ROOT);
        var matchedPlacesByText = places.stream()
                .filter(place -> matchesLocationText(normalizedLocation, place.getName(), place.getAddress(), place.getDescription()))
                .toList();
        var matchedEventsByText = events.stream()
                .filter(event -> matchesLocationText(
                        normalizedLocation,
                        event.getName(),
                        event.getAddress(),
                        event.getDescription(),
                        event.getPlace() != null ? event.getPlace().getAddress() : null,
                        event.getPlace() != null ? event.getPlace().getName() : null
                ))
                .toList();

        var points = new ArrayList<Point>();
        matchedPlacesByText.stream()
                .filter(place -> place.getLat() != null && place.getLng() != null)
                .map(place -> new Point(place.getLat().doubleValue(), place.getLng().doubleValue()))
                .forEach(points::add);
        matchedEventsByText.stream()
                .map(this::resolveEventPoint)
                .flatMap(java.util.Optional::stream)
                .forEach(points::add);

        if (matchedPlacesByText.isEmpty() && matchedEventsByText.isEmpty()) {
            return new FilteredData(
                    List.of(),
                    List.of(),
                    List.of("No entities matched location \"" + location + "\" in the database.")
            );
        }

        if (points.isEmpty()) {
            return new FilteredData(
                    matchedPlacesByText,
                    matchedEventsByText,
                    List.of("Location matched text fields, but geodata is incomplete so radius filter was limited.")
            );
        }

        var centerLat = points.stream().mapToDouble(Point::lat).average().orElse(0d);
        var centerLng = points.stream().mapToDouble(Point::lng).average().orElse(0d);

        var radiusPlaces = places.stream()
                .filter(place -> isWithinRadius(place.getLat(), place.getLng(), centerLat, centerLng, LOCATION_RADIUS_KM))
                .toList();
        var radiusEvents = events.stream()
                .filter(event -> resolveEventPoint(event)
                        .map(point -> haversineKm(centerLat, centerLng, point.lat(), point.lng()) <= LOCATION_RADIUS_KM)
                        .orElse(false))
                .toList();

        var finalPlaces = mergeKeepingOrder(matchedPlacesByText, radiusPlaces);
        var finalEvents = mergeKeepingOrder(matchedEventsByText, radiusEvents);

        return new FilteredData(finalPlaces, finalEvents, List.of());
    }

    private List<Event> filterEventsByDate(ParsedIntent intent, List<Event> events) {
        var routeDate = intent.date() != null ? intent.date() : LocalDate.now();
        return events.stream()
                .filter(event -> event.getStartTime() != null)
                .filter(event -> {
                    var startDate = event.getStartTime().toLocalDate();
                    var endDate = event.getEndTime() != null ? event.getEndTime().toLocalDate() : startDate;
                    return !routeDate.isBefore(startDate) && !routeDate.isAfter(endDate);
                })
                .toList();
    }

    private boolean isAvailablePlace(Place place) {
        if (place == null || place.getId() == null || place.getName() == null || place.getName().isBlank()) {
            return false;
        }
        return isActiveStatus(place.getStatus());
    }

    private boolean isAvailableEvent(Event event) {
        if (event == null || event.getId() == null || event.getName() == null || event.getName().isBlank()) {
            return false;
        }
        return isActiveStatus(event.getStatus());
    }

    private boolean isActiveStatus(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        var normalized = status.toLowerCase(Locale.ROOT);
        return !Set.of("deleted", "archived", "inactive", "draft").contains(normalized);
    }

    private boolean matchesLocationText(String location, String... fields) {
        for (var field : fields) {
            if (field != null && field.toLowerCase(Locale.ROOT).contains(location)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithinRadius(
            java.math.BigDecimal lat,
            java.math.BigDecimal lng,
            double centerLat,
            double centerLng,
            double radiusKm
    ) {
        if (lat == null || lng == null) {
            return false;
        }
        return haversineKm(centerLat, centerLng, lat.doubleValue(), lng.doubleValue()) <= radiusKm;
    }

    private java.util.Optional<Point> resolveEventPoint(Event event) {
        if (event.getLat() != null && event.getLng() != null) {
            return java.util.Optional.of(new Point(event.getLat().doubleValue(), event.getLng().doubleValue()));
        }
        if (event.getPlace() != null && event.getPlace().getLat() != null && event.getPlace().getLng() != null) {
            return java.util.Optional.of(new Point(event.getPlace().getLat().doubleValue(), event.getPlace().getLng().doubleValue()));
        }
        return java.util.Optional.empty();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private <T> List<T> mergeKeepingOrder(List<T> first, List<T> second) {
        var merged = new LinkedHashSet<T>();
        merged.addAll(first);
        merged.addAll(second);
        return List.copyOf(merged);
    }

    private record FilteredData(
            List<Place> places,
            List<Event> events,
            List<String> warnings
    ) {
    }

    private record Point(
            double lat,
            double lng
    ) {
    }
}
