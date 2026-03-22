package ru.fshs.tour.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.catalog.Constraint;

public interface ConstraintRepository extends JpaRepository<Constraint, UUID> {

    Optional<Constraint> findByNameIgnoreCase(String name);
}
