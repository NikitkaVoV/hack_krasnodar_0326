package ru.fshs.tour.assistant.domain.common;

import java.util.List;

/**
 * Normalized search query used by place/event assistant search.
 */
public record ParsedSearchQuery(
        String sourceText,
        String locationText,
        List<String> keywords
) {

    public boolean hasTerms() {
        return (locationText != null && !locationText.isBlank())
                || (keywords != null && !keywords.isEmpty());
    }
}

