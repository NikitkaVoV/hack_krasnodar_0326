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
import ru.fshs.tour.domain.content.PlaceAllergen;
import ru.fshs.tour.repository.PlaceAllergenRepository;

@RestController
@RequestMapping("/api/place-allergens")
@Tag(name = "Place Allergens")
@SecurityRequirement(name = "bearerAuth")
public class PlaceAllergensController extends AbstractCrudController<PlaceAllergen, PlaceAllergenRepository> {

    public PlaceAllergensController(PlaceAllergenRepository repository) {
        super(repository);
    }

    @GetMapping(params = "placeId")
    public List<PlaceAllergen> findAllByPlaceId(@RequestParam UUID placeId) {
        return repository.findAllByPlaceId(placeId);
    }
}
