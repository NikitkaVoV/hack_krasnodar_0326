package ru.fshs.tour.assistant.orchestration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.exception.AssistantException;
import ru.fshs.tour.assistant.exception.AssistantProcessingException;
import ru.fshs.tour.assistant.orchestration.context.AssistantContext;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlerRegistry;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;
import ru.fshs.tour.assistant.orchestration.routing.IntentRouter;

@Service
@RequiredArgsConstructor
public class AssistantOrchestratorImpl implements AssistantOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AssistantOrchestratorImpl.class);

    private final IntentRouter intentRouter;
    private final AssistantHandlerRegistry assistantHandlerRegistry;

    @Override
    public AssistantOrchestrationResult orchestrate(
            ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest request,
            AssistantContext assistantContext
    ) {
        try {
            var resolutionStage = intentRouter.resolveStage(request);
            var handler = assistantHandlerRegistry.resolve(request.getIntent());
            var context = new AssistantHandlingContext(request, assistantContext);
            AssistantSystemResponse response = handler.handle(context);

            return new AssistantOrchestrationResult(
                    response,
                    resolutionStage,
                    handler.getClass().getSimpleName()
            );
        } catch (AssistantException exception) {
            throw exception;
        } catch (Exception exception) {
            String intent = request != null && request.getIntent() != null ? request.getIntent().name() : "UNKNOWN";
            String requestId = request != null && request.getRequestId() != null ? request.getRequestId().toString() : "null";
            String sessionId = request != null && request.getSessionId() != null ? request.getSessionId().toString() : "null";
            log.error(
                    "Assistant orchestration failed [intent={}, requestId={}, sessionId={}]",
                    intent,
                    requestId,
                    sessionId,
                    exception
            );
            String reason = exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();
            throw new AssistantProcessingException(
                    "Failed to orchestrate assistant request: " + reason,
                    exception
            );
        }
    }
}
