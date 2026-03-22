package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.PlaceTransportInfo;

public interface PlaceTransportInfoRepository extends JpaRepository<PlaceTransportInfo, UUID> {

    List<PlaceTransportInfo> findAllByPlaceId(UUID placeId);
}
