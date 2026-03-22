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
import ru.fshs.tour.domain.catalog.Tag;
import ru.fshs.tour.domain.common.BaseEntity;

@Entity
@Table(name = "place_tag", uniqueConstraints = @UniqueConstraint(columnNames = {"place_id", "tag_id"}))
@Getter
@Setter
@NoArgsConstructor
public class PlaceTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
