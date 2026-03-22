package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.domain.catalog.Category;
import ru.fshs.tour.repository.CategoryRepository;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoriesController extends AbstractCrudController<Category, CategoryRepository> {

    public CategoriesController(CategoryRepository repository) {
        super(repository);
    }
}
