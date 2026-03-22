package ru.fshs.tour.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.controller.dto.routes.RouteListItemDto;
import ru.fshs.tour.controller.dto.routes.RouteListResponseDto;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.repository.RouteRepository;

@Service
public class RouteListService {

    private final RouteRepository routeRepository;
    private final RoutePreviewService routePreviewService;

    public RouteListService(RouteRepository routeRepository, RoutePreviewService routePreviewService) {
        this.routeRepository = routeRepository;
        this.routePreviewService = routePreviewService;
    }

    public RouteListResponseDto getRoutesList(String userId, LocalDate date) {
        var normalizedUserId = userId.trim();
        var isPublicScope = normalizedUserId.isBlank() || "public".equalsIgnoreCase(normalizedUserId);

        List<Route> routes;
        if (isPublicScope) {
            routes = routeRepository.findAllByDate(date);
        } else {
            var parsedUserId = parseUserId(normalizedUserId);
            routes = routeRepository.findAllByUserIdAndDate(parsedUserId, date);
        }

        var sorted = routes.stream()
                .sorted(Comparator.comparing(Route::getDate).thenComparing(Route::getId))
                .toList();

        var items = sorted.stream()
                .map(route -> toItem(route, normalizedUserId, isPublicScope))
                .toList();

        return new RouteListResponseDto(items, items.size());
    }

    public RouteListItemDto getRouteListItemById(UUID id) {
        var route = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));
        return toItem(route, "public", true);
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("userId must be 'public' or valid UUID");
        }
    }

    private RouteListItemDto toItem(Route route, String requestedUserId, boolean isPublicScope) {
        var effectiveUser = isPublicScope
                ? "public"
                : route.getUser() != null && route.getUser().getId() != null
                ? route.getUser().getId().toString()
                : requestedUserId;

        var summary = route.getSummary() != null ? route.getSummary() : "";
        var advice = route.getAdvice() != null ? route.getAdvice() : "";
        var imageFields = routePreviewService.resolveRouteImages(route.getId());

        return new RouteListItemDto(
                route.getId() != null ? route.getId().toString() : null,
                effectiveUser,
                route.getDate() != null ? route.getDate().toString() : null,
                route.getTotalDuration() != null ? route.getTotalDuration() : 0,
                summary,
                advice,
                summary.isBlank() ? null : summary,
                advice.isBlank() ? null : advice,
                imageFields.imageUrl(),
                imageFields.coverImage(),
                imageFields.previewImage(),
                imageFields.photos(),
                imageFields.images(),
                null,
                null,
                null,
                deriveBadges(route),
                null,
                null
        );
    }

    private List<String> deriveBadges(Route route) {
        var badges = new ArrayList<String>();
        if (route.getTotalDuration() != null && route.getTotalDuration() >= 300) {
            badges.add("weekend");
        }
        var createdAt = route.getCreatedAt();
        if (createdAt != null && createdAt.isAfter(OffsetDateTime.now().minusDays(14))) {
            badges.add("new");
        }
        if (containsFamilyHint(route)) {
            badges.add("family");
        }
        return badges.isEmpty() ? null : badges;
    }

    private boolean containsFamilyHint(Route route) {
        var text = ((route.getSummary() != null ? route.getSummary() : "") + " "
                + (route.getAdvice() != null ? route.getAdvice() : "")).toLowerCase(Locale.ROOT);
        return text.contains("family") || text.contains("children") || text.contains("сем");
    }
}