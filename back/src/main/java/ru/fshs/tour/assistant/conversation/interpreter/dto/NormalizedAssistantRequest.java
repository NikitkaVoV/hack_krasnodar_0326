package ru.fshs.tour.assistant.conversation.interpreter.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fshs.tour.assistant.model.AssistantIntent;

/**
 * Internal normalized request generated from user message.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedAssistantRequest {

    private UUID requestId;
    private UUID sessionId;
    private UUID userId;
    private AssistantIntent intent;
    private String subIntent;
    private RequestEntitiesDto entities;
    private RequestReferenceDto references;

    @Builder.Default
    private List<String> missingFields = new ArrayList<>();

    private boolean readyForExecution;
    private String originalMessage;
}