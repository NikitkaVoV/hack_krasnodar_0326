package ru.fshs.tour.assistant.domain.common;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.assistant.model.AssistantStatus;

/**
 * Internal structured response produced by domain handlers.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantSystemResponse {

    private AssistantStatus status;
    private AssistantResponseType responseType;
    private String summary;
    private Object payload;

    @Builder.Default
    private List<String> advice = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @Builder.Default
    private List<NextAction> nextActions = new ArrayList<>();
}