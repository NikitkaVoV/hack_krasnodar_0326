package ru.fshs.tour.domain.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.domain.common.BaseEntity;
import ru.fshs.tour.domain.user.User;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
public class Event extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String address;

    private BigDecimal lat;

    private BigDecimal lng;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer recommendedDuration;

    @Column(length = 100)
    private String season;

    private Integer popularity;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Column(nullable = false, length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @OneToMany(mappedBy = "event")
    @JsonIgnore
    private List<EventTag> eventTags = new ArrayList<>();

    @OneToMany(mappedBy = "event")
    @JsonIgnore
    private List<EventCategory> eventCategories = new ArrayList<>();

    @OneToMany(mappedBy = "event")
    @JsonIgnore
    private List<EventConstraint> eventConstraints = new ArrayList<>();
}
