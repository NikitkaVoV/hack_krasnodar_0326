package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.user.UserTag;

public interface UserTagRepository extends JpaRepository<UserTag, UUID> {

    List<UserTag> findAllByUserId(UUID userId);

    List<UserTag> findAllByTagId(UUID tagId);
}
