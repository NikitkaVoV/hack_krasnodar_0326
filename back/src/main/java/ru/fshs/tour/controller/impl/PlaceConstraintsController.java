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
import ru.fshs.tour.domain.content.PlaceConstraint;
import ru.fshs.tour.repository.PlaceConstraintRepository;

@RestController
@RequestMapping("/api/place-constraints")
@Tag(name = "Place Constraints")
@SecurityRequirement(name = "bearerAuth")
public class PlaceConstraintsController extends AbstractCrudController<PlaceConstraint, PlaceConstraintRepository> {

    public PlaceConstraintsController(PlaceConstraintRepository repository) {
        super(repository);
    }

    @GetMapping(params = "placeId")
    public List<PlaceConstraint> findAllByPlaceId(@RequestParam UUID placeId) {
        return repository.findAllByPlaceId(placeId);
    }

    @GetMapping(params = "constraintId")
    public List<PlaceConstraint> findAllByConstraintId(@RequestParam UUID constraintId) {
        return repository.findAllByConstraintId(constraintId);
    }
}
