package ru.fshs.tour.assistant.exception;

/**
 * Thrown when no handler is registered for the resolved intent.
 */
public class UnsupportedAssistantIntentException extends AssistantException {

    public UnsupportedAssistantIntentException(String message) {
        super(message);
    }
}