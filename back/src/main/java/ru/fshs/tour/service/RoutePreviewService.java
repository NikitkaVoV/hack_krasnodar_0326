package ru.fshs.tour.service;

import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.repository.RouteStepRepository;
import ru.fshs.tour.service.model.ImageCompatFields;

@Service
@Transactional(readOnly = true)
public class RoutePreviewService {

    private final RouteStepRepository routeStepRepository;
    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;
    private final ImageCompatService imageCompatService;

    public RoutePreviewService(
            RouteStepRepository routeStepRepository,
            PlaceRepository placeRepository,
            EventRepository eventRepository,
            ImageCompatService imageCompatService
    ) {
        this.routeStepRepository = routeStepRepository;
        this.placeRepository = placeRepository;
        this.eventRepository = eventRepository;
        this.imageCompatService = imageCompatService;
    }

    public ImageCompatFields resolveRouteImages(UUID routeId) {
        var step = routeStepRepository.findFirstByRouteIdOrderByStepOrderAsc(routeId).orElse(null);
        if (step == null || step.getTargetId() == null) {
            return imageCompatService.fromPrimaryUrl(null);
        }

        var targetType = step.getTargetType() != null ? step.getTargetType().toLowerCase(Locale.ROOT) : "";
        if (targetType.contains("place")) {
            var place = placeRepository.findById(step.getTargetId()).orElse(null);
            var placeImage = place == null ? null : place.getMedia().stream()
                    .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : Integer.MAX_VALUE))
                    .map(m -> m.getUrl())
                    .filter(url -> url != null && !url.isBlank())
                    .findFirst()
                    .orElse(null);
            return imageCompatService.fromPrimaryUrl(placeImage);
        }

        if (targetType.contains("event")) {
            var event = eventRepository.findById(step.getTargetId()).orElse(null);
            String eventImage = null;
            if (event != null && event.getPlace() != null) {
                eventImage = event.getPlace().getMedia().stream()
                        .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : Integer.MAX_VALUE))
                        .map(m -> m.getUrl())
                        .filter(url -> url != null && !url.isBlank())
                        .findFirst()
                        .orElse(null);
            }
            return imageCompatService.fromPrimaryUrl(eventImage);
        }

        return imageCompatService.fromPrimaryUrl(null);
    }
}
