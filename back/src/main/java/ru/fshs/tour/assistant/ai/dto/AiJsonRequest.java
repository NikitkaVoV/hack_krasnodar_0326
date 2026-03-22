package ru.fshs.tour.assistant.ai.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiJsonRequest {

    private String systemPrompt;
    private String userPrompt;
    private String jsonSchemaName;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}