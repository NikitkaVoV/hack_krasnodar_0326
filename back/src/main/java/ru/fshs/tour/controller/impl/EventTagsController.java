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
import ru.fshs.tour.domain.content.EventTag;
import ru.fshs.tour.repository.EventTagRepository;

@RestController
@RequestMapping("/api/event-tags")
@Tag(name = "Event Tags")
@SecurityRequirement(name = "bearerAuth")
public class EventTagsController extends AbstractCrudController<EventTag, EventTagRepository> {

    public EventTagsController(EventTagRepository repository) {
        super(repository);
    }

    @GetMapping(params = "eventId")
    public List<EventTag> findAllByEventId(@RequestParam UUID eventId) {
        return repository.findAllByEventId(eventId);
    }

    @GetMapping(params = "tagId")
    public List<EventTag> findAllByTagId(@RequestParam UUID tagId) {
        return repository.findAllByTagId(tagId);
    }
}
