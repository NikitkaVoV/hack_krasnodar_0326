package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.route.VisitHistory;

public interface VisitHistoryRepository extends JpaRepository<VisitHistory, UUID> {

    List<VisitHistory> findAllByUserId(UUID userId);

    List<VisitHistory> findAllByRouteId(UUID routeId);

    List<VisitHistory> findAllByTargetIdAndTargetType(UUID targetId, String targetType);
}
