package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.domain.catalog.Tag;
import ru.fshs.tour.repository.TagRepository;

@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags")
@SecurityRequirement(name = "bearerAuth")
public class TagsController extends AbstractCrudController<Tag, TagRepository> {

    public TagsController(TagRepository repository) {
        super(repository);
    }
}
