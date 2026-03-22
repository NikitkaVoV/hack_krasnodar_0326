package ru.fshs.tour.assistant.model;

/**
 * Top-level assistant intents used by routing layer.
 */
public enum AssistantIntent {
    PLACE_SEARCH,
    PLACE_EXPLAIN,
    EVENT_SEARCH,
    EVENT_EXPLAIN,
    ROUTE_BUILD,
    ROUTE_EDIT,
    ROUTE_EXPLAIN,
    NEARBY_SUGGEST,
    PROFILE_UPDATE,
    GENERAL_QUESTION,
    UNSUPPORTED
}