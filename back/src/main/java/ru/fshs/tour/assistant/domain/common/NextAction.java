package ru.fshs.tour.assistant.domain.common;

/**
 * Next actions the client can offer after assistant response.
 */
public enum NextAction {
    SHOW_ON_MAP,
    SAVE_ROUTE,
    REBUILD_ROUTE,
    OPEN_PLACE,
    OPEN_EVENT,
    ASK_CLARIFICATION,
    RETRY
}