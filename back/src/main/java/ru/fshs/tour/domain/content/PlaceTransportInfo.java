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
@Table(name = "place_transport_info")
@Getter
@Setter
@NoArgsConstructor
public class PlaceTransportInfo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false, length = 50)
    private String transportMode;

    private Integer travelTimeMinutes;

    @Column(columnDefinition = "TEXT")
    private String routeDescription;
}
