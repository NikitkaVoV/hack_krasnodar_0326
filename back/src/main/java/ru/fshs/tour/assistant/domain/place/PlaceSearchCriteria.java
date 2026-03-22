package ru.fshs.tour.assistant.domain.place;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchCriteria {

    private String queryText;
    private String locationText;
    private Double lat;
    private Double lng;
    private Double radiusKm;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private List<String> categories = new ArrayList<>();

    @Builder.Default
    private List<String> constraints = new ArrayList<>();
}
