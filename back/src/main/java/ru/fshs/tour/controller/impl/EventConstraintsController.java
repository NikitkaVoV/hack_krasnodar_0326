package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.domain.content.EventConstraint;
import ru.fshs.tour.repository.EventConstraintRepository;

@RestController
@RequestMapping("/api/event-constraints")
@Tag(name = "Event Constraints")
@SecurityRequirement(name = "bearerAuth")
public class EventConstraintsController extends AbstractCrudController<EventConstraint, EventConstraintRepository> {

    public EventConstraintsController(EventConstraintRepository repository) {
        super(repository);
    }

    @GetMapping(params = "eventId")
    public List<EventConstraint> findAllByEventId(@RequestParam UUID eventId) {
        return repository.findAllByEventId(eventId);
    }

    @GetMapping(params = "constraintId")
    public List<EventConstraint> findAllByConstraintId(@RequestParam UUID constraintId) {
        return repository.findAllByConstraintId(constraintId);
    }
}
