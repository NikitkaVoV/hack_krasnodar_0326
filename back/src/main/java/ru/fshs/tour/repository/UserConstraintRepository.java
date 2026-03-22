package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.user.UserConstraint;

public interface UserConstraintRepository extends JpaRepository<UserConstraint, UUID> {

    List<UserConstraint> findAllByUserId(UUID userId);

    List<UserConstraint> findAllByConstraintId(UUID constraintId);
}
