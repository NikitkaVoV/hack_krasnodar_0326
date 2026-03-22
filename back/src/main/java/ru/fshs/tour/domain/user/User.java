package ru.fshs.tour.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.fshs.tour.domain.common.BaseEntity;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String login;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    @Column(nullable = false, length = 150)
    private String name;

    private Integer age;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String userType = "USER";

    private BigDecimal budgetMin;

    private BigDecimal budgetMax;

    private BigDecimal lastLocationLat;

    private BigDecimal lastLocationLng;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private JsonNode additionalInfo;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    @Builder.Default
    private List<UserTag> userTags = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    @Builder.Default
    private List<UserConstraint> userConstraints = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    @Builder.Default
    private List<UserPreference> preferences = new ArrayList<>();
}
