package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.repository.PlaceRepository;

@RestController
@RequestMapping("/api/places")
@Tag(name = "Places")
@SecurityRequirement(name = "bearerAuth")
public class PlacesController extends AbstractCrudController<Place, PlaceRepository> {

    public PlacesController(PlaceRepository repository) {
        super(repository);
    }

    @GetMapping(params = "tag")
    public List<Place> findAllByTag(@RequestParam String tag) {
        return repository.findAllByTagName(tag);
    }

    @GetMapping(params = "category")
    public List<Place> findAllByCategory(@RequestParam String category) {
        return repository.findAllByCategoryName(category);
    }

    @GetMapping(params = "constraint")
    public List<Place> findAllByConstraint(@RequestParam("constraint") String constraintName) {
        return repository.findAllByConstraintName(constraintName);
    }
}
