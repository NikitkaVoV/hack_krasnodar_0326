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
import ru.fshs.tour.domain.content.EventCategory;
import ru.fshs.tour.repository.EventCategoryRepository;

@RestController
@RequestMapping("/api/event-categories")
@Tag(name = "Event Categories")
@SecurityRequirement(name = "bearerAuth")
public class EventCategoriesController extends AbstractCrudController<EventCategory, EventCategoryRepository> {

    public EventCategoriesController(EventCategoryRepository repository) {
        super(repository);
    }

    @GetMapping(params = "eventId")
    public List<EventCategory> findAllByEventId(@RequestParam UUID eventId) {
        return repository.findAllByEventId(eventId);
    }

    @GetMapping(params = "categoryId")
    public List<EventCategory> findAllByCategoryId(@RequestParam UUID categoryId) {
        return repository.findAllByCategoryId(categoryId);
    }
}
