package ru.fshs.tour.assistant.domain.common;

import java.util.List;
import ru.fshs.tour.assistant.model.AssistantStatus;

/**
 * Common factory methods for deterministic stub responses.
 */
public final class AssistantResultFactory {

    private AssistantResultFactory() {
    }

    public static AssistantSystemResponse notImplemented(AssistantResponseType type, String summary) {
        return AssistantSystemResponse.builder()
                .status(AssistantStatus.PARTIAL)
                .responseType(type)
                .summary(summary)
                .warnings(List.of("Функциональность находится в разработке."))
                .nextActions(List.of(NextAction.RETRY))
                .build();
    }

    public static AssistantSystemResponse unsupported(String summary) {
        return AssistantSystemResponse.builder()
                .status(AssistantStatus.NEEDS_CLARIFICATION)
                .responseType(AssistantResponseType.UNSUPPORTED_REPLY)
                .summary(summary)
                .advice(List.of("Уточните запрос или выберите поддерживаемый сценарий."))
                .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                .build();
    }

    public static AssistantSystemResponse success(
            AssistantResponseType type,
            String summary,
            Object payload,
            List<NextAction> nextActions
    ) {
        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(type)
                .summary(summary)
                .payload(payload)
                .nextActions(nextActions)
                .build();
    }
}