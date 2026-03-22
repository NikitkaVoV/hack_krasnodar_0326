package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.PlaceCategory;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, UUID> {

    List<PlaceCategory> findAllByPlaceId(UUID placeId);

    List<PlaceCategory> findAllByCategoryId(UUID categoryId);
}
