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
@Table(name = "place_infrastructure")
@Getter
@Setter
@NoArgsConstructor
public class PlaceInfrastructure extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private boolean hasFood;

    @Column(nullable = false)
    private boolean hasStay;

    @Column(nullable = false)
    private boolean hasShopsNearby;

    @Column(nullable = false)
    private boolean parkingAvailable;

    @Column(nullable = false)
    private boolean toiletAvailable;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
