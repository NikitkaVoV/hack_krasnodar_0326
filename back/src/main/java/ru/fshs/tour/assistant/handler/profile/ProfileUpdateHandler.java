package ru.fshs.tour.assistant.handler.profile;

import org.springframework.stereotype.Component;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.general.GeneralAssistantService;
import ru.fshs.tour.assistant.handler.AbstractAssistantHandler;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.orchestration.routing.AssistantHandlingContext;

@Component
public class ProfileUpdateHandler extends AbstractAssistantHandler {

    private final GeneralAssistantService generalAssistantService;

    public ProfileUpdateHandler(GeneralAssistantService generalAssistantService) {
        super(AssistantIntent.PROFILE_UPDATE);
        this.generalAssistantService = generalAssistantService;
    }

    @Override
    public AssistantSystemResponse handle(AssistantHandlingContext context) {
        return generalAssistantService.updateProfile(
                context.getRequest().getUserId(),
                context.getRequest().getOriginalMessage(),
                context.getRequest().getEntities()
        );
    }
}
