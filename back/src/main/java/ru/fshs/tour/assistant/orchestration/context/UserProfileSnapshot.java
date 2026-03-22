package ru.fshs.tour.assistant.orchestration.context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lightweight profile copy isolated from heavy domain entities.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSnapshot {

    private UUID userId;
    private Integer age;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private Double lastLat;
    private Double lastLng;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private List<String> constraints = new ArrayList<>();

    @Builder.Default
    private Map<String, String> preferences = new HashMap<>();
}