package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.controller.dto.landing.RouteStepDto;
import ru.fshs.tour.domain.route.RouteStep;
import ru.fshs.tour.repository.RouteStepRepository;
import ru.fshs.tour.service.RouteStepService;

@RestController
@RequestMapping("/api/route-steps")
@Tag(name = "Route Steps")
@SecurityRequirement(name = "bearerAuth")
public class RouteStepsController extends AbstractCrudController<RouteStep, RouteStepRepository> {

    private final RouteStepService routeStepService;

    public RouteStepsController(RouteStepRepository repository, RouteStepService routeStepService) {
        super(repository);
        this.routeStepService = routeStepService;
    }

    @GetMapping(params = "routeId", produces = "application/json; charset=UTF-8")
    @Operation(summary = "Get route steps by route id", security = {})
    @ApiResponse(responseCode = "200", description = "Route steps returned")
    public List<RouteStepDto> findAllByRouteId(@RequestParam @NotNull UUID routeId) {
        return routeStepService.getStepsByRouteId(routeId);
    }

    @Override
    @GetMapping(produces = "application/json; charset=UTF-8")
    @Operation(summary = "Route id parameter is required for route steps", security = {})
    public List<RouteStep> findAll() {
        throw new IllegalArgumentException("routeId is required");
    }
}
