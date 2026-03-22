package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.PlaceMedia;

public interface PlaceMediaRepository extends JpaRepository<PlaceMedia, UUID> {

    List<PlaceMedia> findAllByPlaceId(UUID placeId);
}
