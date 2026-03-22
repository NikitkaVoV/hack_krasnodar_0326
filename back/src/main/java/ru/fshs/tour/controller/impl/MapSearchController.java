package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.dto.map.MapSearchResponseDto;
import ru.fshs.tour.service.MapSearchService;
import ru.fshs.tour.service.model.MapSearchRequest;

@RestController
@RequestMapping("/api/map")
@Validated
@Tag(name = "Map Search")
public class MapSearchController {

    private final MapSearchService mapSearchService;

    public MapSearchController(MapSearchService mapSearchService) {
        this.mapSearchService = mapSearchService;
    }

    @GetMapping(value = "/search", produces = "application/json; charset=UTF-8")
    @Operation(summary = "Search map items around coordinates", security = {})
    @ApiResponse(responseCode = "200", description = "Search completed")
    public MapSearchResponseDto search(
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double lng,
            @RequestParam @NotNull @DecimalMin(value = "0.1", inclusive = true) @DecimalMax("50.0") Double radiusKm,
            @RequestParam(required = false) String types,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) @Min(0) Integer minBudget,
            @RequestParam(required = false) @Min(0) Integer maxBudget,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam(required = false) Boolean openNow,
            @RequestParam(required = false) String suitableFor,
            @RequestParam(required = false) String sortBy
    ) {
        return mapSearchService.search(new MapSearchRequest(
                lat,
                lng,
                radiusKm,
                types,
                categories,
                minBudget,
                maxBudget,
                duration,
                date,
                time,
                openNow,
                suitableFor,
                sortBy
        ));
    }
}
