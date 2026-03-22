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
import ru.fshs.tour.domain.user.UserConstraint;
import ru.fshs.tour.repository.UserConstraintRepository;

@RestController
@RequestMapping("/api/user-constraints")
@Tag(name = "User Constraints")
@SecurityRequirement(name = "bearerAuth")
public class UserConstraintsController extends AbstractCrudController<UserConstraint, UserConstraintRepository> {

    public UserConstraintsController(UserConstraintRepository repository) {
        super(repository);
    }

    @GetMapping(params = "userId")
    public List<UserConstraint> findAllByUserId(@RequestParam UUID userId) {
        return repository.findAllByUserId(userId);
    }

    @GetMapping(params = "constraintId")
    public List<UserConstraint> findAllByConstraintId(@RequestParam UUID constraintId) {
        return repository.findAllByConstraintId(constraintId);
    }
}
