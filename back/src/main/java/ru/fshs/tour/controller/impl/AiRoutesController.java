package ru.fshs.tour.controller.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.dto.routes.AiRouteSaveRequest;
import ru.fshs.tour.controller.dto.routes.AiRouteSaveResponse;
import ru.fshs.tour.service.AiRouteSaveService;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class AiRoutesController {

    private final AiRouteSaveService aiRouteSaveService;

    @PostMapping("/save-from-ai")
    @ResponseStatus(HttpStatus.CREATED)
    public AiRouteSaveResponse saveFromAi(@Valid @RequestBody AiRouteSaveRequest request) {
        return aiRouteSaveService.saveFromAi(request);
    }
}
