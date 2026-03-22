package ru.fshs.tour.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import ru.fshs.tour.domain.route.Route;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    List<Route> findAllByUserId(UUID userId);

    List<Route> findAllByUserIdOrderByDateDescIdDesc(UUID userId);

    List<Route> findAllByDate(LocalDate date);

    List<Route> findAllByUserIdAndDate(UUID userId, LocalDate date);

    Optional<Route> findByIdAndUserId(UUID id, UUID userId);

    List<Route> findAllByUserId(UUID userId, Pageable pageable);

    List<Route> findAllByDateGreaterThanEqualOrderByDateAscIdAsc(LocalDate date, Pageable pageable);

    List<Route> findAllByDateLessThanOrderByDateDescIdDesc(LocalDate date, Pageable pageable);
}
