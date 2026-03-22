package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.domain.catalog.Constraint;
import ru.fshs.tour.repository.ConstraintRepository;

@RestController
@RequestMapping("/api/constraints")
@Tag(name = "Constraints")
@SecurityRequirement(name = "bearerAuth")
public class ConstraintsController extends AbstractCrudController<Constraint, ConstraintRepository> {

    public ConstraintsController(ConstraintRepository repository) {
        super(repository);
    }
}
