package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.dto.routes.RouteListResponseDto;
import ru.fshs.tour.service.MeRoutesService;

@RestController
@RequestMapping("/api/me/routes")
@Tag(name = "My Routes")
@SecurityRequirement(name = "bearerAuth")
public class MeRoutesController {

    private final MeRoutesService meRoutesService;

    public MeRoutesController(MeRoutesService meRoutesService) {
        this.meRoutesService = meRoutesService;
    }

    @GetMapping(produces = "application/json; charset=UTF-8")
    @Operation(summary = "Get current user's routes", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Current user routes returned")
    public RouteListResponseDto getCurrentUserRoutes() {
        return meRoutesService.getCurrentUserRoutes();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete route from current user's history", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Route deleted")
    public void deleteCurrentUserRoute(@PathVariable UUID id) {
        meRoutesService.deleteCurrentUserRoute(id);
    }
}
