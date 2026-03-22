package ru.fshs.tour.domain.route;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.domain.common.BaseEntity;
import ru.fshs.tour.domain.user.User;

@Entity
@Table(name = "route")
@Getter
@Setter
@NoArgsConstructor
public class Route extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    private Integer totalDuration;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @OneToMany(mappedBy = "route")
    @JsonIgnore
    private List<RouteStep> steps = new ArrayList<>();
}
