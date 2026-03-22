package ru.fshs.tour.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.fshs.tour.controller.dto.landing.RouteCollectionDto;
import ru.fshs.tour.controller.dto.landing.RouteDto;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.mapper.LandingMapper;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;

@Service
@RequiredArgsConstructor
public class LandingRouteService {

    private static final int DEFAULT_LIMIT = 6;
    private static final int MAX_LIMIT = 50;
    private static final int PUBLIC_SAMPLE_MULTIPLIER = 5;

    private final RouteRepository routeRepository;
    private final RouteStepRepository routeStepRepository;
    private final LandingMapper landingMapper;

    public List<RouteDto> getPublicRoutes(LocalDate date, int limit) {
        var selectedDate = resolveDate(date);
        var safeLimit = normalizeLimit(limit);
        var candidates = collectPublicCandidates(selectedDate, safeLimit * PUBLIC_SAMPLE_MULTIPLIER);
        return toRouteDtos(orderByDateDistance(candidates, selectedDate), safeLimit);
    }

    /**
     * Route has no dedicated popularity field, so fallback popularity is based on
     * steps count, then nearest date, then stable id ordering.
     */
    public List<RouteDto> getPopularRoutes(LocalDate date, int limit) {
        var selectedDate = resolveDate(date);
        var safeLimit = normalizeLimit(limit);
        var candidates = collectPublicCandidates(selectedDate, safeLimit * PUBLIC_SAMPLE_MULTIPLIER);
        if (candidates.isEmpty()) {
            return List.of();
        }

        var stepCounts = loadStepCounts(candidates);
        var sorted = new ArrayList<>(candidates);
        sorted.sort(Comparator
                .comparingInt((Route route) -> stepCounts.getOrDefault(route.getId(), 0)).reversed()
                .thenComparingLong(route -> distanceInDays(route.getDate(), selectedDate))
                .thenComparing(Route::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Route::getId));
        return toRouteDtos(sorted, safeLimit);
    }

    public List<RouteDto> getRecommendedRoutes(LocalDate date, int limit) {
        return getPublicRoutes(date, limit);
    }

    public List<RouteCollectionDto> getRouteCollections() {
        return List.of(
                new RouteCollectionDto("gastro", "Гастро", "Локальные кафе и рестораны", "gastro"),
                new RouteCollectionDto("one-day", "На один день", "Маршруты на один насыщенный день", "one-day"),
                new RouteCollectionDto("weekend", "На выходные", "Маршруты для поездки на выходные", "weekend"),
                new RouteCollectionDto("short", "Короткие", "Короткие маршруты", "short"),
                new RouteCollectionDto("long", "Долгие", "Долгие маршруты", "long")
        );
    }

    private List<Route> collectPublicCandidates(LocalDate date, int sampleSize) {
        var newer = routeRepository.findAllByDateGreaterThanEqualOrderByDateAscIdAsc(date, PageRequest.of(0, sampleSize));
        if (newer.size() >= sampleSize) {
            return newer;
        }

        var older = routeRepository.findAllByDateLessThanOrderByDateDescIdDesc(
                date,
                PageRequest.of(0, sampleSize - newer.size())
        );
        var merged = new ArrayList<Route>(newer.size() + older.size());
        merged.addAll(newer);
        merged.addAll(older);
        return merged;
    }

    private List<Route> orderByDateDistance(List<Route> routes, LocalDate selectedDate) {
        var sorted = new ArrayList<>(routes);
        sorted.sort(Comparator
                .comparingLong((Route route) -> distanceInDays(route.getDate(), selectedDate))
                .thenComparing(Route::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Route::getId));
        return sorted;
    }

    private long distanceInDays(LocalDate routeDate, LocalDate selectedDate) {
        if (routeDate == null) {
            return Long.MAX_VALUE;
        }
        return Math.abs(ChronoUnit.DAYS.between(selectedDate, routeDate));
    }

    private LocalDate resolveDate(LocalDate date) {
        return date != null ? date : LocalDate.now();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private Map<UUID, Integer> loadStepCounts(List<Route> routes) {
        if (routes.isEmpty()) {
            return Map.of();
        }
        var routeIds = routes.stream().map(Route::getId).toList();
        var counts = new LinkedHashMap<UUID, Integer>();
        routeStepRepository.countByRouteIds(routeIds)
                .forEach(item -> counts.put(item.getRouteId(), (int) item.getStepsCount()));
        return counts;
    }

    private List<RouteDto> toRouteDtos(List<Route> routes, int limit) {
        return routes.stream()
                .limit(limit)
                .map(landingMapper::toRouteDto)
                .toList();
    }
}
