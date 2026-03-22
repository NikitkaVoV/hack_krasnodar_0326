package ru.fshs.tour.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fshs.tour.domain.content.Place;

public interface PlaceRepository extends JpaRepository<Place, UUID> {

    @Query("""
            select distinct p from Place p
            left join fetch p.placeTags pt
            left join fetch pt.tag
            left join fetch p.placeCategories pc
            left join fetch pc.category
            left join fetch p.media m
            where p.lat is not null and p.lng is not null
            """)
    List<Place> findAllForMapSearch();

    @Query("""
            select distinct p from Place p
            join PlaceTag pt on pt.place = p
            join pt.tag t
            where lower(t.name) = lower(:tagName)
            """)
    List<Place> findAllByTagName(@Param("tagName") String tagName);

    @Query("""
            select distinct p from Place p
            join PlaceCategory pc on pc.place = p
            join pc.category c
            where lower(c.name) = lower(:categoryName)
            """)
    List<Place> findAllByCategoryName(@Param("categoryName") String categoryName);

    @Query("""
            select distinct p from Place p
            join PlaceConstraint pc on pc.place = p
            join pc.constraint c
            where lower(c.name) = lower(:constraintName)
            """)
    List<Place> findAllByConstraintName(@Param("constraintName") String constraintName);

    @Query("""
            select p from Place p
            where (:query is null
                or lower(p.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(p.description, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(p.shortDescription, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(p.address, '')) like lower(concat('%', :query, '%')))
            """)
    List<Place> searchForAssistant(@Param("query") String query, Pageable pageable);

    @Query("""
            select p from Place p
            where p.id = :id
            """)
    Optional<Place> findDetailedById(@Param("id") UUID id);
}
