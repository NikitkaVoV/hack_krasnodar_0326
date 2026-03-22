package ru.fshs.tour.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fshs.tour.controller.dto.landing.HomeAggregateDto;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final LandingRouteService landingRouteService;
    private final LandingEventService landingEventService;

    public HomeAggregateDto getHomeData(LocalDate date, int limit) {
        var publicRoutes = landingRouteService.getPublicRoutes(date, limit);
        var popularRoutes = landingRouteService.getPopularRoutes(date, limit);
        var recommendedRoutes = landingRouteService.getRecommendedRoutes(date, limit);
        var upcomingEvents = landingEventService.getUpcomingEvents(date, null, limit);
        var collections = landingRouteService.getRouteCollections();

        return new HomeAggregateDto(
                publicRoutes,
                popularRoutes,
                recommendedRoutes,
                upcomingEvents,
                collections
        );
    }
}
