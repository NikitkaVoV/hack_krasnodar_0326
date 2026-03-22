package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.PlaceAllergen;

public interface PlaceAllergenRepository extends JpaRepository<PlaceAllergen, UUID> {

    List<PlaceAllergen> findAllByPlaceId(UUID placeId);
}
