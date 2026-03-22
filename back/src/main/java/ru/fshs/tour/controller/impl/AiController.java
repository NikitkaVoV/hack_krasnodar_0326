package ru.fshs.tour.controller.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.dto.ai.AiChatRequest;
import ru.fshs.tour.controller.dto.ai.AiChatResponse;
import ru.fshs.tour.service.ai.AiService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    public AiChatResponse chat(@Valid @RequestBody AiChatRequest request) {
        return aiService.chat(request.message());
    }
}
