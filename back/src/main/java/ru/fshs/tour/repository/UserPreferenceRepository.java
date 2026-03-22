package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.user.UserPreference;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    List<UserPreference> findAllByUserId(UUID userId);
}
