package ru.fshs.tour.assistant.conversation.presenter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fshs.tour.assistant.ai.config.AssistantAiProperties;
import ru.fshs.tour.assistant.ai.dto.AiTextRequest;
import ru.fshs.tour.assistant.ai.gateway.AiTextGateway;
import ru.fshs.tour.assistant.conversation.presenter.dto.HumanReadableReply;
import ru.fshs.tour.assistant.conversation.presenter.dto.PresentationInput;
import ru.fshs.tour.assistant.conversation.presenter.prompt.PresentationPromptFactory;
import ru.fshs.tour.assistant.domain.common.AssistantResponseType;
import ru.fshs.tour.assistant.exception.AssistantException;

@Service
@RequiredArgsConstructor
public class SystemResponsePresenterImpl implements SystemResponsePresenter {

    private static final String DEFAULT_REPLY =
            "\u0417\u0430\u043f\u0440\u043e\u0441 \u043f\u0440\u0438\u043d\u044f\u0442. "
                    + "\u041b\u043e\u0433\u0438\u043a\u0430 \u043e\u0442\u0432\u0435\u0442\u0430 "
                    + "\u0431\u0443\u0434\u0435\u0442 \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0430 "
                    + "\u0432 \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0445 \u0438\u0442\u0435\u0440\u0430\u0446\u0438\u044f\u0445.";

    private final AiTextGateway aiTextGateway;
    private final AssistantAiProperties assistantAiProperties;
    private final PresentationPromptFactory presentationPromptFactory;

    @Override
    public HumanReadableReply present(PresentationInput input) {
        if (isRouteResponse(input)) {
            var summary = input.systemResponse() != null ? input.systemResponse().getSummary() : null;
            var text = (summary == null || summary.isBlank())
                    ? "Я подготовил маршрут на основе данных платформы."
                    : summary;
            return new HumanReadableReply(text, "neutral");
        }

        if (assistantAiProperties.isPresentationEnabled()) {
            var aiReply = presentWithAi(input);
            if (aiReply != null && !aiReply.isBlank()) {
                return new HumanReadableReply(aiReply, "neutral");
            }
        }
        String summary = input.systemResponse() != null ? input.systemResponse().getSummary() : null;
        String text = (summary == null || summary.isBlank()) ? DEFAULT_REPLY : summary;
        return new HumanReadableReply(text, "neutral");
    }

    private String presentWithAi(PresentationInput input) {
        var request = AiTextRequest.builder()
                .systemPrompt(presentationPromptFactory.buildSystemPrompt(input))
                .userPrompt(presentationPromptFactory.buildUserPrompt(input))
                .build();
        try {
            return aiTextGateway.generateText(request);
        } catch (AssistantException ignored) {
            return null;
        }
    }

    private boolean isRouteResponse(PresentationInput input) {
        if (input == null || input.systemResponse() == null || input.systemResponse().getResponseType() == null) {
            return false;
        }
        var type = input.systemResponse().getResponseType();
        return type == AssistantResponseType.ROUTE_PROPOSAL
                || type == AssistantResponseType.ROUTE_UPDATED
                || type == AssistantResponseType.ROUTE_DETAILS;
    }
}
