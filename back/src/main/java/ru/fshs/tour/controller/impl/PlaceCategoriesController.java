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
import ru.fshs.tour.domain.content.PlaceCategory;
import ru.fshs.tour.repository.PlaceCategoryRepository;

@RestController
@RequestMapping("/api/place-categories")
@Tag(name = "Place Categories")
@SecurityRequirement(name = "bearerAuth")
public class PlaceCategoriesController extends AbstractCrudController<PlaceCategory, PlaceCategoryRepository> {

    public PlaceCategoriesController(PlaceCategoryRepository repository) {
        super(repository);
    }

    @GetMapping(params = "placeId")
    public List<PlaceCategory> findAllByPlaceId(@RequestParam UUID placeId) {
        return repository.findAllByPlaceId(placeId);
    }

    @GetMapping(params = "categoryId")
    public List<PlaceCategory> findAllByCategoryId(@RequestParam UUID categoryId) {
        return repository.findAllByCategoryId(categoryId);
    }
}
