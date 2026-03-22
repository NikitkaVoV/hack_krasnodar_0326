package ru.fshs.tour.assistant.model;

/**
 * Normalized status of assistant processing result.
 */
public enum AssistantStatus {
    SUCCESS,
    PARTIAL,
    NEEDS_CLARIFICATION,
    FAILED
}