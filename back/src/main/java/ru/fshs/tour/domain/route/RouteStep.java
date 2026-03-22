package ru.fshs.tour.domain.route;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.domain.common.BaseEntity;

@Entity
@Table(name = "route_step")
@Getter
@Setter
@NoArgsConstructor
public class RouteStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false, length = 20)
    private String targetType;

    private Integer stepOrder;

    private LocalDateTime plannedTimeStart;

    private LocalDateTime plannedTimeEnd;

    private Integer travelTimeMinutes;

    private Integer waitTimeMinutes;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 50)
    private String transportMode;

    private Integer priority;
}
