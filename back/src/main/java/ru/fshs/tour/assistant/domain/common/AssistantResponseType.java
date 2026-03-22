package ru.fshs.tour.assistant.domain.common;

/**
 * Structured response payload type for downstream presentation.
 */
public enum AssistantResponseType {
    PLACE_LIST,
    PLACE_DETAILS,
    EVENT_LIST,
    EVENT_DETAILS,
    ROUTE_PROPOSAL,
    ROUTE_UPDATED,
    ROUTE_DETAILS,
    NEARBY_LIST,
    PROFILE_ACK,
    GENERAL_REPLY,
    UNSUPPORTED_REPLY
}