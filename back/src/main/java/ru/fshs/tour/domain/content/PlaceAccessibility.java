package ru.fshs.tour.domain.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.domain.common.BaseEntity;

@Entity
@Table(name = "place_accessibility")
@Getter
@Setter
@NoArgsConstructor
public class PlaceAccessibility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private boolean accessibleForDisabled;

    @Column(nullable = false)
    private boolean accessibleForChildren;

    @Column(nullable = false)
    private boolean wheelchairAccess;

    @Column(nullable = false)
    private boolean seniorFriendly;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
