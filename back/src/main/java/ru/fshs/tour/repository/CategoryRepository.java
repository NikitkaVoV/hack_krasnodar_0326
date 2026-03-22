package ru.fshs.tour.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.catalog.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findBySlugIgnoreCase(String slug);
}
