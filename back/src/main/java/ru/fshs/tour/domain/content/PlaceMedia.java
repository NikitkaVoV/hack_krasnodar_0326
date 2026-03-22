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
@Table(name = "place_media")
@Getter
@Setter
@NoArgsConstructor
public class PlaceMedia extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false, length = 50)
    private String mediaType;

    @Column(nullable = false, length = 500)
    private String url;

    private Integer sortOrder;
}
