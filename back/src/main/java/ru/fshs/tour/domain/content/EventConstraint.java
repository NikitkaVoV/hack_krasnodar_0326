package ru.fshs.tour.domain.content;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.domain.catalog.Constraint;
import ru.fshs.tour.domain.common.BaseEntity;

@Entity
@Table(name = "event_constraint", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "constraint_id"}))
@Getter
@Setter
@NoArgsConstructor
public class EventConstraint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "constraint_id", nullable = false)
    private Constraint constraint;
}
