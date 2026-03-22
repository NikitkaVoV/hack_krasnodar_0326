package ru.fshs.tour.assistant.conversation.interpreter.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Structured output expected from the model for request interpretation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInterpretationResult {

    private String intent;
    private String subIntent;
    private RequestEntitiesDto entities;
    private RequestReferenceDto references;

    @Builder.Default
    private List<String> missingFields = new ArrayList<>();

    private Boolean readyForExecution;
}