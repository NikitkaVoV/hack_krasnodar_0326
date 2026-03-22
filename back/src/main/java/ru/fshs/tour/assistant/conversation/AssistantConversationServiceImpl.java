package ru.fshs.tour.assistant.conversation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.assistant.api.dto.AssistantDebugDto;
import ru.fshs.tour.assistant.api.dto.AssistantHumanReplyDto;
import ru.fshs.tour.assistant.api.dto.AssistantMessageRequest;
import ru.fshs.tour.assistant.api.dto.AssistantMessageResponse;
import ru.fshs.tour.assistant.conversation.interpreter.UserMessageInterpreter;
import ru.fshs.tour.assistant.conversation.interpreter.dto.AssistantInterpretationInput;
import ru.fshs.tour.assistant.conversation.presenter.SystemResponsePresenter;
import ru.fshs.tour.assistant.conversation.presenter.dto.PresentationInput;
import ru.fshs.tour.assistant.exception.AssistantProcessingException;
import ru.fshs.tour.assistant.orchestration.AssistantOrchestrator;
import ru.fshs.tour.assistant.orchestration.context.AssistantContextService;
import ru.fshs.tour.service.CurrentUserService;

@Service
@RequiredArgsConstructor
public class AssistantConversationServiceImpl implements AssistantConversationService {

    private final DialogSessionService dialogSessionService;
    private final DialogMessageService dialogMessageService;
    private final AssistantContextService assistantContextService;
    private final UserMessageInterpreter userMessageInterpreter;
    private final AssistantOrchestrator assistantOrchestrator;
    private final SystemResponsePresenter systemResponsePresenter;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public AssistantMessageResponse processMessage(AssistantMessageRequest request) {
        validateRequest(request);
        var currentUserId = currentUserService.getCurrentUser().getId();

        var session = dialogSessionService.getOrCreate(request.sessionId(), currentUserId);
        dialogMessageService.saveUserMessage(session, request.message());

        var assistantContext = assistantContextService.buildContext(currentUserId, request.clientContext());

        var interpretationInput = new AssistantInterpretationInput(
                request.message(),
                session.getId(),
                currentUserId,
                request.clientContext(),
                assistantContext.getUserProfile(),
                assistantContext.getActiveRouteId(),
                assistantContext.getCurrentPlaceId(),
                assistantContext.getCurrentEventId()
        );

        var normalizedRequest = userMessageInterpreter.interpret(interpretationInput);
        normalizedRequest.setSessionId(session.getId());
        normalizedRequest.setUserId(currentUserId);

        var orchestrationResult = assistantOrchestrator.orchestrate(normalizedRequest, assistantContext);
        var humanReply = systemResponsePresenter.present(
                new PresentationInput(normalizedRequest, orchestrationResult.systemResponse())
        );

        dialogMessageService.saveAssistantMessage(
                session,
                humanReply.text(),
                normalizedRequest,
                orchestrationResult.systemResponse()
        );
        dialogSessionService.updateLastIntent(session, normalizedRequest.getIntent());

        return new AssistantMessageResponse(
                session.getId(),
                new AssistantHumanReplyDto(humanReply.text(), humanReply.tone()),
                normalizedRequest,
                orchestrationResult.systemResponse(),
                new AssistantDebugDto(
                        normalizedRequest.getIntent() != null ? normalizedRequest.getIntent().name() : null,
                        orchestrationResult.resolutionStage() != null ? orchestrationResult.resolutionStage().name() : null,
                        orchestrationResult.handlerName()
                )
        );
    }

    private void validateRequest(AssistantMessageRequest request) {
        if (request == null) {
            throw new AssistantProcessingException("Assistant request must not be null");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new AssistantProcessingException("message must not be blank");
        }
    }
}
