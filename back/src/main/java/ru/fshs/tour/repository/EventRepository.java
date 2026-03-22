package ru.fshs.tour.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fshs.tour.domain.content.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("""
            select distinct e from Event e
            left join fetch e.eventTags et
            left join fetch et.tag
            left join fetch e.eventCategories ec
            left join fetch ec.category
            left join fetch e.place p
            left join fetch p.media pm
            where (e.lat is not null and e.lng is not null)
               or (p.lat is not null and p.lng is not null)
            """)
    List<Event> findAllForMapSearch();

    @Query("""
            select distinct e from Event e
            join EventTag et on et.event = e
            join et.tag t
            where lower(t.name) = lower(:tagName)
            """)
    List<Event> findAllByTagName(@Param("tagName") String tagName);

    @Query("""
            select distinct e from Event e
            join EventCategory ec on ec.event = e
            join ec.category c
            where lower(c.name) = lower(:categoryName)
            """)
    List<Event> findAllByCategoryName(@Param("categoryName") String categoryName);

    @Query("""
            select distinct e from Event e
            join EventConstraint ec on ec.event = e
            join ec.constraint c
            where lower(c.name) = lower(:constraintName)
            """)
    List<Event> findAllByConstraintName(@Param("constraintName") String constraintName);

    List<Event> findAllByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            LocalDateTime from,
            LocalDateTime toExclusive,
            Pageable pageable
    );

    @Query("""
            select distinct e from Event e
            left join e.place p
            where (:query is null
                or lower(e.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(e.description, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(e.address, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(p.name, '')) like lower(concat('%', :query, '%')))
            """)
    List<Event> searchForAssistant(@Param("query") String query, Pageable pageable);

    @Query("""
            select e from Event e
            where e.id = :id
            """)
    Optional<Event> findDetailedById(@Param("id") UUID id);
}
