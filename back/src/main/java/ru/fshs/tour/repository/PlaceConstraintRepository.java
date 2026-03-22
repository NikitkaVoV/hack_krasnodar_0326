package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.PlaceConstraint;

public interface PlaceConstraintRepository extends JpaRepository<PlaceConstraint, UUID> {

    List<PlaceConstraint> findAllByPlaceId(UUID placeId);

    List<PlaceConstraint> findAllByConstraintId(UUID constraintId);
}
