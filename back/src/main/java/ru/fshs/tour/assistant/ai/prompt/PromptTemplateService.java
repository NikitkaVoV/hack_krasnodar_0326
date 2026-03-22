package ru.fshs.tour.assistant.ai.prompt;

import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Lightweight prompt templating utility.
 */
@Service
public class PromptTemplateService {

    public String apply(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}