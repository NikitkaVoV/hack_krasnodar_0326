package ru.fshs.tour.assistant.ai.support;

import org.springframework.stereotype.Component;

/**
 * Provides JSON schema for structured AI outputs.
 */
@Component
public class JsonSchemaProvider {

    public String schemaFor(Class<?> responseType) {
        return "{}";
    }
}