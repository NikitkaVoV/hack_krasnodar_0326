package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.EventTag;

public interface EventTagRepository extends JpaRepository<EventTag, UUID> {

    List<EventTag> findAllByEventId(UUID eventId);

    List<EventTag> findAllByTagId(UUID tagId);
}
