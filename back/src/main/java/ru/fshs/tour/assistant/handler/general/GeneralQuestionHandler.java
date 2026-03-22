package ru.fshs.tour.assistant.handler.general;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.general.GeneralAssistantService;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class GeneralQuestionHandler extends AbstractAssistantHandler {

    private final GeneralAssistantService generalAssistantService;

    public GeneralQuestionHandler(GeneralAssistantService generalAssistantService) {
        super(AssistantIntent.GENERAL_QUESTION);
        this.generalAssistantService = generalAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        return generalAssistantService.answerQuestion(context.getRequest().getOriginalMessage());
    }
}