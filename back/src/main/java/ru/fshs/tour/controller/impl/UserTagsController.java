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
import ru.fshs.tour.domain.user.UserTag;
import ru.fshs.tour.repository.UserTagRepository;

@RestController
@RequestMapping("/api/user-tags")
@Tag(name = "User Tags")
@SecurityRequirement(name = "bearerAuth")
public class UserTagsController extends AbstractCrudController<UserTag, UserTagRepository> {

    public UserTagsController(UserTagRepository repository) {
        super(repository);
    }

    @GetMapping(params = "userId")
    public List<UserTag> findAllByUserId(@RequestParam UUID userId) {
        return repository.findAllByUserId(userId);
    }

    @GetMapping(params = "tagId")
    public List<UserTag> findAllByTagId(@RequestParam UUID tagId) {
        return repository.findAllByTagId(tagId);
    }
}
