package ru.fshs.tour.assistant.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.assistant.api.dto.AssistantMessageRequest;
import ru.fshs.tour.assistant.api.dto.AssistantMessageResponse;
import ru.fshs.tour.assistant.conversation.AssistantConversationService;

/**
 * Assistant API transport controller.
 */
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantConversationService assistantConversationService;

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.OK)
    public AssistantMessageResponse sendMessage(@Valid @RequestBody AssistantMessageRequest request) {
        return assistantConversationService.processMessage(request);
    }
}