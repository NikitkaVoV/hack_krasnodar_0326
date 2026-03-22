package ru.fshs.tour.service.ai;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.domain.route.RouteStep;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;
import ru.fshs.tour.service.ai.model.GeneratedRoute;
import ru.fshs.tour.service.ai.model.GeneratedRouteStep;
import ru.fshs.tour.service.ai.model.ParsedIntent;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteBuilderService {

    private static final int MIN_STEP_DURATION = 45;
    private static final int DEFAULT_STEP_DURATION = 60;
    private static final int MAX_STEPS = 6;
    private static final int DEFAULT_HOP_LIMIT_KM = 25;
    private static final int NO_WALKING_HOP_LIMIT_KM = 6;
    private static final int DEFAULT_TRAVEL_MINUTES = 15;

    private final RouteRepository routeRepository;
    private final RouteStepRepository routeStepRepository;
    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;

    public GeneratedRoute buildRoute(ParsedIntent intent, List<Place> places, List<Event> events) {
        var warnings = new ArrayList<String>();
        var date = intent.date() != null ? intent.date() : LocalDate.now();
        var durationLimit = Math.max(MIN_STEP_DURATION, intent.durationMinutes());
        var noLongWalking = intent.constraints().contains("no_long_walking");
        var hopLimit = noLongWalking ? NO_WALKING_HOP_LIMIT_KM : DEFAULT_HOP_LIMIT_KM;

        var candidates = new ArrayList<Candidate>();
        places.stream()
                .map(place -> toPlaceCandidate(place, intent))
                .flatMap(java.util.Optional::stream)
                .forEach(candidates::add);
        events.stream()
                .map(event -> toEventCandidate(event, intent, date))
                .flatMap(java.util.Optional::stream)
                .forEach(candidates::add);

        candidates.sort(Comparator.comparingDouble(Candidate::baseScore).reversed());
        if (candidates.isEmpty()) {
            warnings.add("No places or events matched your request in the current database.");
            return new GeneratedRoute(
                    "No route could be generated from available data.",
                    0,
                    List.of(),
                    List.copyOf(warnings),
                    false
            );
        }

        var plannedSteps = new ArrayList<GeneratedRouteStep>();
        var usedIds = new LinkedHashSet<UUID>();
        var currentTime = date.atTime(9, 0);
        int consumedMinutes = 0;
        Candidate previous = null;

        while (plannedSteps.size() < MAX_STEPS) {
            Candidate selected = null;
            int selectedTravel = 0;
            int selectedWait = 0;
            double selectedRank = Double.NEGATIVE_INFINITY;

            for (var candidate : candidates) {
                if (usedIds.contains(candidate.id())) {
                    continue;
                }

                var travelMinutes = estimateTravelMinutes(previous, candidate);
                var distanceKm = previous != null ? distanceKm(previous, candidate) : 0d;
                if (previous != null && distanceKm > hopLimit) {
                    continue;
                }

                var arrivalTime = currentTime.plusMinutes(travelMinutes);
                var waitMinutes = 0;
                if (candidate.type() == CandidateType.EVENT && candidate.startTime() != null && arrivalTime.isBefore(candidate.startTime())) {
                    waitMinutes = (int) Duration.between(arrivalTime, candidate.startTime()).toMinutes();
                    arrivalTime = candidate.startTime();
                }

                if (candidate.type() == CandidateType.EVENT && candidate.endTime() != null) {
                    var plannedEnd = arrivalTime.plusMinutes(candidate.durationMinutes());
                    if (plannedEnd.isAfter(candidate.endTime())) {
                        continue;
                    }
                }

                var budgetAfter = consumedMinutes + travelMinutes + waitMinutes + candidate.durationMinutes();
                if (budgetAfter > durationLimit) {
                    continue;
                }

                var dynamicRank = candidate.baseScore() - travelMinutes * 0.6 - waitMinutes * 0.25;
                if (dynamicRank > selectedRank) {
                    selected = candidate;
                    selectedRank = dynamicRank;
                    selectedTravel = travelMinutes;
                    selectedWait = waitMinutes;
                }
            }

            if (selected == null) {
                break;
            }

            var stepStart = currentTime.plusMinutes(selectedTravel + selectedWait);
            var stepEnd = stepStart.plusMinutes(selected.durationMinutes());
            plannedSteps.add(new GeneratedRouteStep(
                    selected.type().jsonType,
                    selected.id(),
                    selected.title(),
                    selected.durationMinutes(),
                    stepStart,
                    stepEnd,
                    selected.lat(),
                    selected.lng(),
                    selected.address()
            ));

            consumedMinutes += selectedTravel + selectedWait + selected.durationMinutes();
            currentTime = stepEnd;
            previous = selected;
            usedIds.add(selected.id());
        }

        if (plannedSteps.isEmpty()) {
            warnings.add("No route steps passed time and distance constraints.");
        }

        var summary = plannedSteps.isEmpty()
                ? "No route could be generated from available data."
                : "Route for %s: %d stops, %d minutes."
                .formatted(date, plannedSteps.size(), consumedMinutes);

        return new GeneratedRoute(summary, consumedMinutes, List.copyOf(plannedSteps), List.copyOf(warnings), false);
    }

    public GeneratedRoute buildFallbackRoute(ParsedIntent intent) {
        var pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));
        var routes = routeRepository.findAll(pageRequest).getContent();
        if (routes.isEmpty()) {
            return new GeneratedRoute(
                    "No fallback routes are available in the database.",
                    0,
                    List.of(),
                    List.of("No ready-made routes were found."),
                    true
            );
        }

        var durationLimit = Math.max(MIN_STEP_DURATION, intent.durationMinutes());
        var sortedRoutes = routes.stream()
                .sorted(Comparator.comparingInt(route -> distanceToDuration(route, durationLimit)))
                .toList();

        for (var route : sortedRoutes) {
            var generated = convertSavedRoute(route, durationLimit, intent.date());
            if (!generated.isEmpty()) {
                return generated;
            }
        }

        return new GeneratedRoute(
                "No fallback route could satisfy duration constraints.",
                0,
                List.of(),
                List.of("Saved routes exist, but none fit your requested duration."),
                true
        );
    }

    private GeneratedRoute convertSavedRoute(Route route, int durationLimit, LocalDate date) {
        var steps = routeStepRepository.findAllByRouteIdOrderByStepOrderAsc(route.getId());
        if (steps.isEmpty()) {
            return new GeneratedRoute(
                    "Fallback route has no steps.",
                    0,
                    List.of(),
                    List.of("A fallback route was found but has no steps."),
                    true
            );
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

        Map<UUID, Place> placesById = placeRepository.findAllById(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, place -> place));
        Map<UUID, Event> eventsById = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, event -> event));

        var routeDate = date != null ? date : (route.getDate() != null ? route.getDate() : LocalDate.now());
        var currentTime = routeDate.atTime(9, 0);
        var generatedSteps = new ArrayList<GeneratedRouteStep>();
        var consumed = 0;

        for (var step : steps) {
            var resolved = resolveSavedStep(step, placesById, eventsById, currentTime);
            if (resolved == null) {
                continue;
            }
            if (resolved.durationMinutes() < MIN_STEP_DURATION) {
                continue;
            }
            if (consumed + resolved.durationMinutes() > durationLimit) {
                break;
            }

            generatedSteps.add(resolved);
            consumed += resolved.durationMinutes();
            currentTime = resolved.plannedEnd();
        }

        if (generatedSteps.isEmpty()) {
            return new GeneratedRoute(
                    "Fallback route had no valid steps.",
                    0,
                    List.of(),
                    List.of("Fallback route steps are not usable with current constraints."),
                    true
            );
        }

        var summary = route.getSummary() != null && !route.getSummary().isBlank()
                ? route.getSummary()
                : "Fallback route from existing saved route %s".formatted(route.getId());

        return new GeneratedRoute(
                summary,
                consumed,
                List.copyOf(generatedSteps),
                List.of("Showing the closest existing saved route."),
                true
        );
    }

    private GeneratedRouteStep resolveSavedStep(
            RouteStep step,
            Map<UUID, Place> placesById,
            Map<UUID, Event> eventsById,
            LocalDateTime currentTime
    ) {
        if (isPlaceType(step.getTargetType())) {
            var place = placesById.get(step.getTargetId());
            if (place == null) {
                return null;
            }
            var duration = normalizeDuration(place.getRecommendedDuration());
            var end = currentTime.plusMinutes(duration);
            return new GeneratedRouteStep(
                    "place",
                    place.getId(),
                    place.getName(),
                    duration,
                    currentTime,
                    end,
                    place.getLat(),
                    place.getLng(),
                    place.getAddress()
            );
        }

        if (isEventType(step.getTargetType())) {
            var event = eventsById.get(step.getTargetId());
            if (event == null) {
                return null;
            }
            var duration = normalizeDuration(event.getRecommendedDuration());
            var start = currentTime;
            if (event.getStartTime() != null && event.getStartTime().isAfter(start)) {
                start = event.getStartTime();
            }
            var end = start.plusMinutes(duration);
            if (event.getEndTime() != null && end.isAfter(event.getEndTime())) {
                return null;
            }
            var lat = event.getLat() != null ? event.getLat() : (event.getPlace() != null ? event.getPlace().getLat() : null);
            var lng = event.getLng() != null ? event.getLng() : (event.getPlace() != null ? event.getPlace().getLng() : null);
            var address = event.getAddress() != null ? event.getAddress() : (event.getPlace() != null ? event.getPlace().getAddress() : null);
            return new GeneratedRouteStep(
                    "event",
                    event.getId(),
                    event.getName(),
                    duration,
                    start,
                    end,
                    lat,
                    lng,
                    address
            );
        }

        return null;
    }

    private java.util.Optional<Candidate> toPlaceCandidate(Place place, ParsedIntent intent) {
        if (place.getId() == null || place.getName() == null || place.getName().isBlank()) {
            return java.util.Optional.empty();
        }
        var tokens = collectPlaceTokens(place);
        if (!matchesHardConstraints(tokens, intent.constraints())) {
            return java.util.Optional.empty();
        }
        var score = calculateScore(tokens, intent.preferences(), place.getPopularity(), false);
        return java.util.Optional.of(new Candidate(
                CandidateType.PLACE,
                place.getId(),
                place.getName(),
                normalizeDuration(place.getRecommendedDuration()),
                place.getLat(),
                place.getLng(),
                place.getAddress(),
                null,
                null,
                score
        ));
    }

    private java.util.Optional<Candidate> toEventCandidate(Event event, ParsedIntent intent, LocalDate routeDate) {
        if (event.getId() == null || event.getName() == null || event.getName().isBlank()) {
            return java.util.Optional.empty();
        }
        if (event.getStartTime() == null) {
            return java.util.Optional.empty();
        }

        if (!eventMatchesDate(event, routeDate)) {
            return java.util.Optional.empty();
        }

        var tokens = collectEventTokens(event);
        if (!matchesHardConstraints(tokens, intent.constraints())) {
            return java.util.Optional.empty();
        }

        var lat = event.getLat() != null ? event.getLat() : (event.getPlace() != null ? event.getPlace().getLat() : null);
        var lng = event.getLng() != null ? event.getLng() : (event.getPlace() != null ? event.getPlace().getLng() : null);
        var address = event.getAddress() != null ? event.getAddress() : (event.getPlace() != null ? event.getPlace().getAddress() : null);

        var score = calculateScore(tokens, intent.preferences(), event.getPopularity(), true);
        return java.util.Optional.of(new Candidate(
                CandidateType.EVENT,
                event.getId(),
                event.getName(),
                normalizeDuration(event.getRecommendedDuration()),
                lat,
                lng,
                address,
                event.getStartTime(),
                event.getEndTime(),
                score
        ));
    }

    private boolean eventMatchesDate(Event event, LocalDate date) {
        var start = event.getStartTime();
        if (start == null) {
            return false;
        }
        var end = event.getEndTime();
        var startDate = start.toLocalDate();
        var endDate = end != null ? end.toLocalDate() : startDate;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    private boolean matchesHardConstraints(Set<String> tokens, List<String> constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return true;
        }
        for (var constraint : constraints) {
            if ("no_long_walking".equals(constraint)) {
                continue;
            }
            if ("wheelchair_access".equals(constraint)
                    && !containsAny(tokens, "wheelchair", "accessible", "доступ", "коляск")) {
                return false;
            }
            if ("family_friendly".equals(constraint)
                    && !containsAny(tokens, "family", "kids", "children", "дет")) {
                return false;
            }
            if ("indoor_only".equals(constraint)
                    && !containsAny(tokens, "indoor", "inside", "museum", "галере", "театр", "помещен")) {
                return false;
            }
            if ("outdoor_only".equals(constraint)
                    && !containsAny(tokens, "park", "outdoor", "outside", "улиц", "open air", "nature")) {
                return false;
            }
        }
        return true;
    }

    private double calculateScore(Set<String> tokens, List<String> preferences, Integer popularity, boolean eventCandidate) {
        double score = Math.min(100, popularity != null ? popularity : 0);
        if (preferences != null) {
            for (var preference : preferences) {
                score += switch (preference) {
                    case "nature" -> containsAny(tokens, "nature", "park", "forest", "природ") ? 35 : 0;
                    case "animals" -> containsAny(tokens, "animal", "zoo", "horse", "живот", "ферм") ? 35 : 0;
                    case "culture" -> containsAny(tokens, "culture", "art", "театр", "галере") ? 30 : 0;
                    case "history" -> containsAny(tokens, "history", "museum", "historic", "музей", "истор") ? 30 : 0;
                    case "food" -> containsAny(tokens, "food", "cafe", "restaurant", "еда", "кухн") ? 25 : 0;
                    case "family" -> containsAny(tokens, "family", "kids", "children", "дет") ? 20 : 0;
                    default -> 0;
                };
            }
        }
        if (eventCandidate) {
            score += 10;
        }
        return score;
    }

    private Set<String> collectPlaceTokens(Place place) {
        var tokens = new LinkedHashSet<String>();
        addTokens(tokens, place.getName());
        addTokens(tokens, place.getDescription());
        addTokens(tokens, place.getShortDescription());
        addTokens(tokens, place.getAddress());
        place.getPlaceTags().forEach(tag -> addTokens(tokens, tag.getTag().getName()));
        place.getPlaceCategories().forEach(category -> addTokens(tokens, category.getCategory().getName()));
        place.getPlaceConstraints().forEach(constraint -> addTokens(tokens, constraint.getConstraint().getName()));
        return tokens;
    }

    private Set<String> collectEventTokens(Event event) {
        var tokens = new LinkedHashSet<String>();
        addTokens(tokens, event.getName());
        addTokens(tokens, event.getDescription());
        addTokens(tokens, event.getAddress());
        event.getEventTags().forEach(tag -> addTokens(tokens, tag.getTag().getName()));
        event.getEventCategories().forEach(category -> addTokens(tokens, category.getCategory().getName()));
        event.getEventConstraints().forEach(constraint -> addTokens(tokens, constraint.getConstraint().getName()));
        if (event.getPlace() != null) {
            addTokens(tokens, event.getPlace().getName());
            addTokens(tokens, event.getPlace().getAddress());
        }
        return tokens;
    }

    private void addTokens(Set<String> target, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        for (var part : raw.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+")) {
            if (part.length() >= 3) {
                target.add(part);
            }
        }
    }

    private int normalizeDuration(Integer rawDuration) {
        if (rawDuration == null) {
            return DEFAULT_STEP_DURATION;
        }
        return Math.max(MIN_STEP_DURATION, rawDuration);
    }

    private int estimateTravelMinutes(Candidate from, Candidate to) {
        if (from == null || to == null) {
            return 0;
        }
        if (from.lat() == null || from.lng() == null || to.lat() == null || to.lng() == null) {
            return DEFAULT_TRAVEL_MINUTES;
        }
        var distanceKm = distanceKm(from, to);
        if (distanceKm <= 0.2d) {
            return 5;
        }
        var minutes = (int) Math.round((distanceKm / 22d) * 60d);
        return Math.max(6, Math.min(90, minutes));
    }

    private double distanceKm(Candidate from, Candidate to) {
        if (from == null || to == null || from.lat() == null || from.lng() == null || to.lat() == null || to.lng() == null) {
            return 0d;
        }
        return haversineKm(
                from.lat().doubleValue(),
                from.lng().doubleValue(),
                to.lat().doubleValue(),
                to.lng().doubleValue()
        );
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

    private boolean isPlaceType(String rawType) {
        return rawType != null && rawType.toLowerCase(Locale.ROOT).contains("place");
    }

    private boolean isEventType(String rawType) {
        return rawType != null && rawType.toLowerCase(Locale.ROOT).contains("event");
    }

    private int distanceToDuration(Route route, int requestedDuration) {
        var routeDuration = route.getTotalDuration() != null ? route.getTotalDuration() : requestedDuration;
        return Math.abs(routeDuration - requestedDuration);
    }

    private boolean containsAny(Set<String> tokens, String... options) {
        for (var option : options) {
            var normalized = option.toLowerCase(Locale.ROOT);
            if (tokens.contains(normalized)) {
                return true;
            }
            for (var token : tokens) {
                if (token.contains(normalized) || normalized.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private enum CandidateType {
        PLACE("place"),
        EVENT("event");

        private final String jsonType;

        CandidateType(String jsonType) {
            this.jsonType = jsonType;
        }
    }

    private record Candidate(
            CandidateType type,
            UUID id,
            String title,
            int durationMinutes,
            BigDecimal lat,
            BigDecimal lng,
            String address,
            LocalDateTime startTime,
            LocalDateTime endTime,
            double baseScore
    ) {
    }
}
