package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.PlaceInfrastructure;

public interface PlaceInfrastructureRepository extends JpaRepository<PlaceInfrastructure, UUID> {

    List<PlaceInfrastructure> findAllByPlaceId(UUID placeId);
}
