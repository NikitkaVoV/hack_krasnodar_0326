package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.EventConstraint;

public interface EventConstraintRepository extends JpaRepository<EventConstraint, UUID> {

    List<EventConstraint> findAllByEventId(UUID eventId);

    List<EventConstraint> findAllByConstraintId(UUID constraintId);
}
