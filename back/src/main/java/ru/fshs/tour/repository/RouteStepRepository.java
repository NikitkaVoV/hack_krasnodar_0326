package ru.fshs.tour.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fshs.tour.domain.route.RouteStep;

public interface RouteStepRepository extends JpaRepository<RouteStep, UUID> {

    List<RouteStep> findAllByRouteId(UUID routeId);

    List<RouteStep> findAllByRouteIdOrderByStepOrderAsc(UUID routeId);

    void deleteAllByRouteId(UUID routeId);

    Optional<RouteStep> findFirstByRouteIdOrderByStepOrderAsc(UUID routeId);

    long countByRouteId(UUID routeId);

    @Query("""
            select rs.route.id as routeId, count(rs.id) as stepsCount
            from RouteStep rs
            where rs.route.id in :routeIds
            group by rs.route.id
            """)
    List<RouteStepCountView> countByRouteIds(@Param("routeIds") List<UUID> routeIds);

    interface RouteStepCountView {
        UUID getRouteId();

        long getStepsCount();
    }
}
