package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.PlaceTag;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, UUID> {

    List<PlaceTag> findAllByPlaceId(UUID placeId);

    List<PlaceTag> findAllByTagId(UUID tagId);
}
