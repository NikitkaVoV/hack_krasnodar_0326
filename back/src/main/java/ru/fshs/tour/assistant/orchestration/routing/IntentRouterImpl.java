package ru.fshs.tour.assistant.orchestration.routing;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;
import ru.fshs.tour.assistant.model.AssistantIntent;

@Component
public class IntentRouterImpl implements IntentRouter {

    @Override
    public ResolutionStage resolveStage(NormalizedAssistantRequest request) {
        if (request == null || request.getIntent() == AssistantIntent.UNSUPPORTED) {
            return ResolutionStage.UNSUPPORTED_FALLBACK;
        }
        return ResolutionStage.INTENT_ROUTED;
    }
}