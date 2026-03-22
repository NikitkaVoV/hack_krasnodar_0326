package ru.fshs.tour.controller.dto.landing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Homepage aggregate payload")
public class HomeAggregateDto {

    private List<RouteDto> publicRoutes = new ArrayList<>();
    private List<RouteDto> popularRoutes = new ArrayList<>();
    private List<RouteDto> recommendedRoutes = new ArrayList<>();
    private List<EventDto> upcomingEvents = new ArrayList<>();
    private List<RouteCollectionDto> collections = new ArrayList<>();

    public HomeAggregateDto() {
    }

    public HomeAggregateDto(
            List<RouteDto> publicRoutes,
            List<RouteDto> popularRoutes,
            List<RouteDto> recommendedRoutes,
            List<EventDto> upcomingEvents,
            List<RouteCollectionDto> collections
    ) {
        this.publicRoutes = publicRoutes != null ? publicRoutes : new ArrayList<>();
        this.popularRoutes = popularRoutes != null ? popularRoutes : new ArrayList<>();
        this.recommendedRoutes = recommendedRoutes != null ? recommendedRoutes : new ArrayList<>();
        this.upcomingEvents = upcomingEvents != null ? upcomingEvents : new ArrayList<>();
        this.collections = collections != null ? collections : new ArrayList<>();
    }

    public List<RouteDto> getPublicRoutes() {
        return publicRoutes;
    }

    public List<RouteDto> getPopularRoutes() {
        return popularRoutes;
    }

    public List<RouteDto> getRecommendedRoutes() {
        return recommendedRoutes;
    }

    public List<EventDto> getUpcomingEvents() {
        return upcomingEvents;
    }

    public List<RouteCollectionDto> getCollections() {
        return collections;
    }
}
