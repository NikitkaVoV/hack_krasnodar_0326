package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.dto.route_steps.RouteStepEventDto;
import ru.fshs.tour.controller.dto.route_steps.RouteStepPlaceDto;
import ru.fshs.tour.service.RouteStepTargetService;

@RestController
@RequestMapping("/api/route-steps")
@Tag(name = "Route Step Targets")
public class RouteStepTargetsController {

    private final RouteStepTargetService routeStepTargetService;

    public RouteStepTargetsController(RouteStepTargetService routeStepTargetService) {
        this.routeStepTargetService = routeStepTargetService;
    }

    @GetMapping(value = "/places/{id}", produces = "application/json; charset=UTF-8")
    @Operation(summary = "Get place details for route step", security = {})
    @ApiResponse(responseCode = "200", description = "Place details returned")
    public RouteStepPlaceDto getPlaceById(@PathVariable String id) {
        return routeStepTargetService.getPlaceDetailsById(id);
    }

    @GetMapping(value = "/events/{id}", produces = "application/json; charset=UTF-8")
    @Operation(summary = "Get event details for route step", security = {})
    @ApiResponse(responseCode = "200", description = "Event details returned")
    public RouteStepEventDto getEventById(@PathVariable String id) {
        return routeStepTargetService.getEventDetailsById(id);
    }
}
