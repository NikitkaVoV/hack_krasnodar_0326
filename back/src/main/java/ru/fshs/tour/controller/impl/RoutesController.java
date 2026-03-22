package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.controller.dto.landing.RouteCollectionDto;
import ru.fshs.tour.controller.dto.landing.RouteDto;
import ru.fshs.tour.controller.dto.routes.RouteListItemDto;
import ru.fshs.tour.controller.dto.routes.RouteListResponseDto;
import ru.fshs.tour.domain.route.Route;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.service.LandingRouteService;
import ru.fshs.tour.service.RouteListService;

@RestController
@RequestMapping("/api/routes")
@Tag(name = "Routes")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class RoutesController extends AbstractCrudController<Route, RouteRepository> {

    private final LandingRouteService landingRouteService;
    private final RouteListService routeListService;

    public RoutesController(
            RouteRepository repository,
            LandingRouteService landingRouteService,
            RouteListService routeListService
    ) {
        super(repository);
        this.landingRouteService = landingRouteService;
        this.routeListService = routeListService;
    }

    @GetMapping(params = "userId")
    public List<Route> findAllByUserId(@RequestParam UUID userId) {
        return repository.findAllByUserId(userId);
    }

    @GetMapping(params = "date")
    public List<Route> findAllByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return repository.findAllByDate(date);
    }

    @GetMapping("/public")
    @Operation(summary = "Get public routes for homepage", security = {})
    @ApiResponse(responseCode = "200", description = "Public routes returned")
    public List<RouteDto> publicRoutes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "6") @Min(1) @Max(50) Integer limit
    ) {
        return landingRouteService.getPublicRoutes(date, limit);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular routes for homepage", security = {})
    @ApiResponse(responseCode = "200", description = "Popular routes returned")
    public List<RouteDto> popularRoutes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "6") @Min(1) @Max(50) Integer limit
    ) {
        return landingRouteService.getPopularRoutes(date, limit);
    }

    @GetMapping("/recommended")
    @Operation(summary = "Get recommended routes for homepage", security = {})
    @ApiResponse(responseCode = "200", description = "Recommended routes returned")
    public List<RouteDto> recommendedRoutes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "6") @Min(1) @Max(50) Integer limit
    ) {
        return landingRouteService.getRecommendedRoutes(date, limit);
    }

    @GetMapping("/collections")
    @Operation(summary = "Get route collections for homepage", security = {})
    @ApiResponse(responseCode = "200", description = "Route collections returned")
    public List<RouteCollectionDto> collections() {
        return landingRouteService.getRouteCollections();
    }

    @GetMapping(value = "/list", produces = "application/json; charset=UTF-8")
    @Operation(summary = "Get routes catalog list by user scope and date", security = {})
    @ApiResponse(responseCode = "200", description = "Routes list returned")
    public RouteListResponseDto listRoutes(
            @RequestParam @NotBlank String userId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return routeListService.getRoutesList(userId, date);
    }

    @GetMapping(value = "/list/{id}", produces = "application/json; charset=UTF-8")
    @Operation(summary = "Get one route meta item by id", security = {})
    @ApiResponse(responseCode = "200", description = "Route meta returned")
    public RouteListItemDto getRouteListItemById(@PathVariable UUID id) {
        return routeListService.getRouteListItemById(id);
    }
}
