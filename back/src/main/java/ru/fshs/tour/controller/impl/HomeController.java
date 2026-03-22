package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.dto.landing.HomeAggregateDto;
import ru.fshs.tour.service.HomeService;

@RestController
@RequestMapping("/api/home")
@Tag(name = "Home")
@Validated
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "Get aggregated home data for homepage", security = {})
    @ApiResponse(responseCode = "200", description = "Home payload returned")
    public HomeAggregateDto getHomeData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "6") @Min(1) @Max(50) Integer limit
    ) {
        return homeService.getHomeData(date, limit);
    }
}
