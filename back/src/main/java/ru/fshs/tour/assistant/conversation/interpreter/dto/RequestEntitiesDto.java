package ru.fshs.tour.assistant.conversation.interpreter.dto;

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
public class RequestEntitiesDto {

    private String date;
    private String dateFrom;
    private String dateTo;
    private String groupType;
    private String pace;
    private String budgetLevel;
    private String transportMode;
    private String locationText;
    private Double lat;
    private Double lng;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private List<String> categories = new ArrayList<>();

    @Builder.Default
    private List<String> constraints = new ArrayList<>();
}