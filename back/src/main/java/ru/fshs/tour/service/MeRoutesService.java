package ru.fshs.tour.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.controller.dto.routes.RouteListItemDto;
import ru.fshs.tour.controller.dto.routes.RouteListResponseDto;
import ru.fshs.tour.exception.RouteForbiddenException;
import ru.fshs.tour.exception.RouteNotFoundException;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;

@Service
@Transactional(readOnly = true)
public class MeRoutesService {

    private final RouteRepository routeRepository;
    private final CurrentUserService currentUserService;
    private final RoutePreviewService routePreviewService;
    private final RouteStepRepository routeStepRepository;

    public MeRoutesService(
            RouteRepository routeRepository,
            CurrentUserService currentUserService,
            RoutePreviewService routePreviewService,
            RouteStepRepository routeStepRepository
    ) {
        this.routeRepository = routeRepository;
        this.currentUserService = currentUserService;
        this.routePreviewService = routePreviewService;
        this.routeStepRepository = routeStepRepository;
    }

    public RouteListResponseDto getCurrentUserRoutes() {
        var userId = currentUserService.getCurrentUser().getId();
        var items = routeRepository.findAllByUserIdOrderByDateDescIdDesc(userId).stream()
                .map(route -> {
                    var imageFields = routePreviewService.resolveRouteImages(route.getId());
                    return new RouteListItemDto(
                            route.getId() != null ? route.getId().toString() : null,
                            userId.toString(),
                            route.getDate() != null ? route.getDate().toString() : null,
                            route.getTotalDuration() != null ? route.getTotalDuration() : 0,
                            route.getSummary() != null ? route.getSummary() : "",
                            route.getAdvice() != null ? route.getAdvice() : "",
                            route.getSummary(),
                            route.getAdvice(),
                            imageFields.imageUrl(),
                            imageFields.coverImage(),
                            imageFields.previewImage(),
                            imageFields.photos(),
                            imageFields.images(),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );
                })
                .toList();
        return new RouteListResponseDto(items, items.size());
    }

    @Transactional
    public void deleteCurrentUserRoute(UUID routeId) {
        var userId = currentUserService.getCurrentUser().getId();

        var route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Маршрут не найден"));

        if (route.getUser() == null || route.getUser().getId() == null || !userId.equals(route.getUser().getId())) {
            throw new RouteForbiddenException("Нет доступа к маршруту");
        }

        routeStepRepository.deleteAllByRouteId(routeId);
        routeRepository.delete(route);
    }
}
