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
import ru.fshs.tour.domain.user.UserPreference;
import ru.fshs.tour.repository.UserPreferenceRepository;

@RestController
@RequestMapping("/api/user-preferences")
@Tag(name = "User Preferences")
@SecurityRequirement(name = "bearerAuth")
public class UserPreferencesController extends AbstractCrudController<UserPreference, UserPreferenceRepository> {

    public UserPreferencesController(UserPreferenceRepository repository) {
        super(repository);
    }

    @GetMapping(params = "userId")
    public List<UserPreference> findAllByUserId(@RequestParam UUID userId) {
        return repository.findAllByUserId(userId);
    }
}
