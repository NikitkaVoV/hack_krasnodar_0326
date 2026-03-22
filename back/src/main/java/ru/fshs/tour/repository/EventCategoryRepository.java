package ru.fshs.tour.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fshs.tour.domain.content.EventCategory;

public interface EventCategoryRepository extends JpaRepository<EventCategory, UUID> {

    List<EventCategory> findAllByEventId(UUID eventId);

    List<EventCategory> findAllByCategoryId(UUID categoryId);
}
