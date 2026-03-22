package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.PlaceAccessibility;

public interface PlaceAccessibilityRepository extends JpaRepository<PlaceAccessibility, UUID> {

    List<PlaceAccessibility> findAllByPlaceId(UUID placeId);
}
