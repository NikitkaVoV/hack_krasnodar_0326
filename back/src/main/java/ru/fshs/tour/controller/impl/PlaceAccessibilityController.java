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
import ru.fshs.tour.domain.content.PlaceAccessibility;
import ru.fshs.tour.repository.PlaceAccessibilityRepository;

@RestController
@RequestMapping("/api/place-accessibility")
@Tag(name = "Place Accessibility")
@SecurityRequirement(name = "bearerAuth")
public class PlaceAccessibilityController extends AbstractCrudController<PlaceAccessibility, PlaceAccessibilityRepository> {

    public PlaceAccessibilityController(PlaceAccessibilityRepository repository) {
        super(repository);
    }

    @GetMapping(params = "placeId")
    public List<PlaceAccessibility> findAllByPlaceId(@RequestParam UUID placeId) {
        return repository.findAllByPlaceId(placeId);
    }
}
