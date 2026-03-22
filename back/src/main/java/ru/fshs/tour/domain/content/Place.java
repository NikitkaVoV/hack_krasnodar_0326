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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.domain.common.BaseEntity;
import ru.fshs.tour.domain.user.User;

@Entity
@Table(name = "place")
@Getter
@Setter
@NoArgsConstructor
public class Place extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String shortDescription;

    @Column(length = 255)
    private String address;

    private BigDecimal lat;

    private BigDecimal lng;

    private Integer recommendedDuration;

    @Column(length = 255)
    private String openingHours;

    @Column(length = 100)
    private String season;

    private Integer popularity;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Column(nullable = false)
    private boolean has3d;

    @Column(nullable = false)
    private boolean hasVr;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false, length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @OneToMany(mappedBy = "place")
    @JsonIgnore
    private List<PlaceTag> placeTags = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    @JsonIgnore
    private List<PlaceCategory> placeCategories = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    @JsonIgnore
    private List<PlaceConstraint> placeConstraints = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    @JsonIgnore
    private List<PlaceMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    @JsonIgnore
    private List<PlaceAllergen> allergens = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    @JsonIgnore
    private List<PlaceTransportInfo> transportInfo = new ArrayList<>();
}
