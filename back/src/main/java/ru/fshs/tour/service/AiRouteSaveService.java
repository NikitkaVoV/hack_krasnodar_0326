package ru.fshs.tour.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.controller.dto.routes.AiRouteDraftStepDto;
import ru.fshs.tour.controller.dto.routes.AiRouteSaveRequest;
import ru.fshs.tour.controller.dto.routes.AiRouteSaveResponse;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.domain.route.RouteStep;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;
import ru.fshs.tour.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AiRouteSaveService {

    private static final int MIN_STEP_DURATION = 45;
    private static final int DEFAULT_STEP_DURATION = 60;

    private final RouteRepository routeRepository;
    private final RouteStepRepository routeStepRepository;
    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public AiRouteSaveResponse saveFromAi(AiRouteSaveRequest request) {
        if (request == null || request.route() == null || request.route().steps() == null || request.route().steps().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route payload is required");
        }
        if (Boolean.FALSE.equals(request.route().canBeSaved())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route cannot be saved");
        }

        var currentUser = currentUserService.getCurrentUser();
        var user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        var routeDate = parseDateOrDefault(request.route().date(), LocalDate.now());

        var route = new Route();
        route.setUser(user);
        route.setDate(routeDate);
        route.setSummary(request.route().summary());
        route.setAdvice(request.route().advice());
        route.setTotalDuration(request.route().totalDuration());
        route = routeRepository.save(route);

        int totalDuration = 0;
        LocalDateTime cursor = routeDate.atTime(9, 0);
        int order = 1;
        for (var step : request.route().steps()) {
            var targetId = parseTargetId(step.id());
            var targetType = normalizeTargetType(step.type());
            validateTargetExists(targetType, targetId);

            var duration = normalizeDuration(step.duration());
            var start = parseTime(routeDate, step.plannedTimeStart(), cursor);
            var end = parseTime(routeDate, step.plannedTimeEnd(), start.plusMinutes(duration));
            if (!end.isAfter(start)) {
                end = start.plusMinutes(duration);
            }

            var routeStep = new RouteStep();
            routeStep.setRoute(route);
            routeStep.setTargetId(targetId);
            routeStep.setTargetType(targetType);
            routeStep.setStepOrder(step.orderIndex() != null ? step.orderIndex() : order);
            routeStep.setPlannedTimeStart(start);
            routeStep.setPlannedTimeEnd(end);
            routeStep.setTravelTimeMinutes(0);
            routeStep.setWaitTimeMinutes(0);
            routeStep.setNotes(firstNonBlank(step.description(), step.title()));
            routeStep.setPriority(order);
            routeStepRepository.save(routeStep);

            totalDuration += duration;
            cursor = end;
            order++;
        }

        if (route.getTotalDuration() == null || route.getTotalDuration() <= 0) {
            route.setTotalDuration(totalDuration);
            routeRepository.save(route);
        }

        return new AiRouteSaveResponse(
                route.getId().toString(),
                "Маршрут сохранён"
        );
    }

    private String normalizeTargetType(String rawType) {
        if (rawType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Step type is required");
        }
        var normalized = rawType.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("place")) {
            return "place";
        }
        if (normalized.contains("event")) {
            return "event";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported step type: " + rawType);
    }

    private void validateTargetExists(String targetType, UUID targetId) {
        boolean exists = "place".equals(targetType)
                ? placeRepository.existsById(targetId)
                : eventRepository.existsById(targetId);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Step target does not exist: " + targetId);
        }
    }

    private UUID parseTargetId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Step id is required");
        }
        var normalized = rawId.trim().replaceFirst("(?i)^(place_|event_|p-|e-)", "");
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid step id: " + rawId);
        }
    }

    private int normalizeDuration(Integer rawDuration) {
        if (rawDuration == null) {
            return DEFAULT_STEP_DURATION;
        }
        return Math.max(MIN_STEP_DURATION, rawDuration);
    }

    private LocalDate parseDateOrDefault(String rawDate, LocalDate fallback) {
        if (rawDate == null || rawDate.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException ex) {
            return fallback;
        }
    }

    private LocalDateTime parseTime(LocalDate date, String rawTime, LocalDateTime fallback) {
        if (rawTime == null || rawTime.isBlank()) {
            return fallback;
        }
        try {
            return LocalDateTime.of(date, LocalTime.parse(rawTime));
        } catch (DateTimeParseException ex) {
            return fallback;
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
