package ru.fshs.tour.assistant.exception;

/**
 * Controlled marker exception for planned but not yet implemented features.
 */
public class NotImplementedAssistantFeatureException extends AssistantException {

    public NotImplementedAssistantFeatureException(String message) {
        super(message);
    }
}