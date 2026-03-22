package ru.fshs.tour.domain.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.domain.common.BaseEntity;

@Entity
@Table(name = "constraint_rule")
@Getter
@Setter
@NoArgsConstructor
public class Constraint extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String constraintType;

    @Column(columnDefinition = "TEXT")
    private String description;
}
