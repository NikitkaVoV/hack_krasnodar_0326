package ru.fshs.tour.assistant.domain.common;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parser that extracts meaningful search tokens and location from natural text.
 */
public final class AssistantSearchTextParser {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "(?:\\b(?:in|near|around)\\b|\\b(?:\\u0432|\\u0432\\u043e|\\u0438\\u0437)\\b)\\s+([\\p{L}\\-]{2,}(?:\\s+[\\p{L}\\-]{2,}){0,2})",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "what", "which", "where", "show", "find",
            "for", "with", "and", "or", "to", "of", "please", "me",
            "place", "places", "event", "events", "route", "routes",
            "popular", "top", "best", "rating", "highest",
            "\u043a\u0430\u043a\u0438\u0435", "\u043a\u0430\u043a\u043e\u0435", "\u043a\u0430\u043a", "\u0433\u0434\u0435",
            "\u0435\u0441\u0442\u044c", "\u043f\u043e\u043a\u0430\u0436\u0438", "\u043d\u0430\u0439\u0434\u0438", "\u043c\u043d\u0435",
            "\u0432", "\u0432\u043e", "\u0438\u0437", "\u043d\u0430", "\u0434\u043b\u044f", "\u0438", "\u0438\u043b\u0438",
            "\u043c\u0435\u0441\u0442\u043e", "\u043c\u0435\u0441\u0442\u0430", "\u043c\u0435\u0441\u0442",
            "\u0441\u043e\u0431\u044b\u0442\u0438\u0435", "\u0441\u043e\u0431\u044b\u0442\u0438\u044f", "\u043c\u0430\u0440\u0448\u0440\u0443\u0442", "\u043c\u0430\u0440\u0448\u0440\u0443\u0442\u044b",
            "\u043f\u043e\u043f\u0443\u043b\u044f\u0440\u043d\u044b\u0439", "\u043f\u043e\u043f\u0443\u043b\u044f\u0440\u043d\u044b\u0435", "\u043f\u043e\u043f\u0443\u043b\u044f\u0440\u043d\u044b\u0445",
            "\u0442\u043e\u043f", "\u043b\u0443\u0447\u0448\u0438\u0439", "\u043b\u0443\u0447\u0448\u0438\u0435", "\u043b\u0443\u0447\u0448\u0438\u0445"
    );

    private AssistantSearchTextParser() {
    }

    public static ParsedSearchQuery parse(String rawMessage, String explicitLocation) {
        String source = normalize(rawMessage);
        String location = normalize(explicitLocation);
        if (location == null && source != null) {
            location = extractLocation(source);
        }

        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        if (source != null) {
            for (String token : SPLIT_PATTERN.split(source)) {
                String normalizedToken = normalize(token);
                if (normalizedToken == null) {
                    continue;
                }
                if (normalizedToken.length() < 2) {
                    continue;
                }
                if (STOP_WORDS.contains(normalizedToken)) {
                    continue;
                }
                keywords.add(normalizedToken);
            }
        }

        if (location != null) {
            for (String token : SPLIT_PATTERN.split(location)) {
                String normalizedToken = normalize(token);
                if (normalizedToken != null) {
                    keywords.remove(normalizedToken);
                }
            }
        }

        return new ParsedSearchQuery(source, location, List.copyOf(keywords));
    }

    public static boolean containsIgnoreCase(String value, String term) {
        if (value == null || term == null || term.isBlank()) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT));
    }

    private static String extractLocation(String source) {
        var matcher = LOCATION_PATTERN.matcher(source);
        if (!matcher.find()) {
            return null;
        }
        return normalize(matcher.group(1));
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }
}
