package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.controller.dto.landing.EventDto;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.service.LandingEventService;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class EventsController extends AbstractCrudController<Event, EventRepository> {

    private final LandingEventService landingEventService;

    public EventsController(EventRepository repository, LandingEventService landingEventService) {
        super(repository);
        this.landingEventService = landingEventService;
    }

    @GetMapping(params = "tag")
    public List<Event> findAllByTag(@RequestParam String tag) {
        return repository.findAllByTagName(tag);
    }

    @GetMapping(params = "category")
    public List<Event> findAllByCategory(@RequestParam String category) {
        return repository.findAllByCategoryName(category);
    }

    @GetMapping(params = "constraint")
    public List<Event> findAllByConstraint(@RequestParam("constraint") String constraintName) {
        return repository.findAllByConstraintName(constraintName);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events for homepage", security = {})
    @ApiResponse(responseCode = "200", description = "Upcoming events returned")
    public List<EventDto> upcomingEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "6") @Min(1) @Max(50) Integer limit
    ) {
        return landingEventService.getUpcomingEvents(dateFrom, dateTo, limit);
    }
}
