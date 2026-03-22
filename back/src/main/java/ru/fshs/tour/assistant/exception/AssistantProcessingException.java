package ru.fshs.tour.assistant.exception;

/**
 * Thrown when assistant pipeline fails during processing.
 */
public class AssistantProcessingException extends AssistantException {

    public AssistantProcessingException(String message) {
        super(message);
    }

    public AssistantProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}