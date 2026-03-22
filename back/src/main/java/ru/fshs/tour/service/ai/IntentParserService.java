package ru.fshs.tour.service.ai;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import ru.fshs.tour.service.ai.model.ParsedIntent;

@Service
public class IntentParserService {

    private static final int DEFAULT_DURATION_MINUTES = 300;
    private static final int MIN_DURATION_MINUTES = 60;
    private static final int MAX_DURATION_MINUTES = 720;

    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b");
    private static final Pattern DOT_DATE_PATTERN = Pattern.compile("\\b(\\d{1,2})[./](\\d{1,2})[./](20\\d{2})\\b");

    private static final Pattern LOCATION_EN_PATTERN = Pattern.compile("\\b(?:in|near|at)\\s+([\\p{L}][\\p{L}\\s-]{1,60})");
    private static final Pattern LOCATION_RU_PATTERN = Pattern.compile("\\b(?:в|около|рядом с)\\s+([\\p{L}][\\p{L}\\s-]{1,60})");

    private static final Pattern HOURS_PATTERN = Pattern.compile("(\\d{1,2})\\s*(?:h|hr|hrs|hour|hours|ч|час(?:а|ов)?)");
    private static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d{1,3})\\s*(?:min|mins|minutes|m|мин(?:ут)?)");
    private static final Pattern RAW_MINUTES_PATTERN = Pattern.compile("\\bfor\\s+(\\d{2,3})\\b");

    private static final Pattern RIDING_EN_PATTERN = Pattern.compile("\\b([\\p{L}]+\\s+riding)\\b");
    private static final Pattern RIDING_RU_PATTERN = Pattern.compile("\\b(катани[ея]\\s+на\\s+[\\p{L}]+)\\b");
    private static final Pattern WANT_PATTERN = Pattern.compile("\\b(?:want|looking for|хочу|ищу)\\s+([\\p{L}][\\p{L}\\s-]{2,40})\\b");

    public ParsedIntent parse(String message) {
        var safeMessage = message == null ? "" : message.trim();
        var lower = safeMessage.toLowerCase(Locale.ROOT);

        var preferences = extractPreferences(lower);
        var constraints = extractConstraints(lower);

        return new ParsedIntent(
                "build_route",
                safeMessage,
                extractLocation(safeMessage, lower),
                extractDate(lower),
                extractDuration(lower),
                preferences,
                constraints,
                extractBudget(lower),
                extractRequestedActivities(lower)
        );
    }

    private String extractLocation(String originalMessage, String lowerMessage) {
        var location = extractLocationByPattern(originalMessage, lowerMessage, LOCATION_EN_PATTERN);
        if (location != null) {
            return location;
        }
        return extractLocationByPattern(originalMessage, lowerMessage, LOCATION_RU_PATTERN);
    }

    private String extractLocationByPattern(String originalMessage, String lowerMessage, Pattern pattern) {
        var matcher = pattern.matcher(lowerMessage);
        if (!matcher.find()) {
            return null;
        }

        var raw = originalMessage.substring(matcher.start(1), matcher.end(1));
        var normalized = raw.replaceAll("[,.;!?]", " ").replaceAll("\\s{2,}", " ").trim();
        if (normalized.isBlank()) {
            return null;
        }

        normalized = normalized.replaceAll("(?i)\\b(?:on|for|with|без|на|с|и)\\b.*$", "").trim();
        normalized = normalized.replaceAll("\\b\\d{4}-\\d{2}-\\d{2}\\b.*$", "").trim();
        normalized = normalized.replaceAll("\\b\\d{1,2}[./]\\d{1,2}[./]\\d{4}\\b.*$", "").trim();
        normalized = normalized.replaceAll("(?i)\\b(?:today|tomorrow|сегодня|завтра)\\b.*$", "").trim();

        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60).trim();
        }
        return normalized.isBlank() ? null : normalized;
    }

    private LocalDate extractDate(String message) {
        var isoMatcher = ISO_DATE_PATTERN.matcher(message);
        if (isoMatcher.find()) {
            try {
                return LocalDate.parse(isoMatcher.group(1));
            } catch (DateTimeParseException ignored) {
                // no-op
            }
        }

        var dotMatcher = DOT_DATE_PATTERN.matcher(message);
        if (dotMatcher.find()) {
            try {
                var day = Integer.parseInt(dotMatcher.group(1));
                var month = Integer.parseInt(dotMatcher.group(2));
                var year = Integer.parseInt(dotMatcher.group(3));
                return LocalDate.of(year, month, day);
            } catch (RuntimeException ignored) {
                // no-op
            }
        }

        if (message.contains("tomorrow") || message.contains("завтра")) {
            return LocalDate.now().plusDays(1);
        }
        if (message.contains("today") || message.contains("сегодня")) {
            return LocalDate.now();
        }
        return null;
    }

    private int extractDuration(String message) {
        int minutes = 0;

        var hoursMatcher = HOURS_PATTERN.matcher(message);
        while (hoursMatcher.find()) {
            minutes += Integer.parseInt(hoursMatcher.group(1)) * 60;
        }

        var minutesMatcher = MINUTES_PATTERN.matcher(message);
        while (minutesMatcher.find()) {
            minutes += Integer.parseInt(minutesMatcher.group(1));
        }

        if (minutes == 0) {
            var rawMatcher = RAW_MINUTES_PATTERN.matcher(message);
            if (rawMatcher.find()) {
                minutes = Integer.parseInt(rawMatcher.group(1));
            }
        }

        if (minutes == 0) {
            if (message.contains("half day") || message.contains("полдня")) {
                minutes = 240;
            } else if (message.contains("full day") || message.contains("целый день")) {
                minutes = 480;
            } else {
                minutes = DEFAULT_DURATION_MINUTES;
            }
        }

        return Math.max(MIN_DURATION_MINUTES, Math.min(MAX_DURATION_MINUTES, minutes));
    }

    private List<String> extractPreferences(String message) {
        var preferences = new LinkedHashSet<String>();
        addIfContains(preferences, message, "nature", "nature", "природ", "park", "forest");
        addIfContains(preferences, message, "animals", "animals", "animal", "zoo", "horse", "живот");
        addIfContains(preferences, message, "culture", "culture", "art", "theatre", "театр", "искус");
        addIfContains(preferences, message, "history", "history", "historic", "museum", "музей", "истор");
        addIfContains(preferences, message, "food", "food", "restaurant", "cafe", "еда", "кухн");
        addIfContains(preferences, message, "family", "family", "kids", "children", "дет");
        return List.copyOf(preferences);
    }

    private List<String> extractConstraints(String message) {
        var constraints = new LinkedHashSet<String>();
        addIfContains(
                constraints,
                message,
                "no_long_walking",
                "no long walking",
                "without long walking",
                "don't walk much",
                "без долгих прогулок",
                "мало ходить"
        );
        addIfContains(constraints, message, "wheelchair_access", "wheelchair", "accessible", "коляск");
        addIfContains(constraints, message, "indoor_only", "indoor", "inside", "в помещении");
        addIfContains(constraints, message, "outdoor_only", "outdoor", "outside", "на улице");
        addIfContains(constraints, message, "family_friendly", "family", "kids", "children", "дет");
        return List.copyOf(constraints);
    }

    private String extractBudget(String message) {
        if (containsAny(message, "cheap", "low budget", "economy", "дешев", "бюджет")) {
            return "low";
        }
        if (containsAny(message, "luxury", "premium", "high budget", "дорог", "люкс")) {
            return "high";
        }
        if (containsAny(message, "medium", "mid-range", "средн")) {
            return "medium";
        }
        return "medium";
    }

    private List<String> extractRequestedActivities(String message) {
        Set<String> activities = new LinkedHashSet<>();

        addPatternMatches(activities, message, RIDING_EN_PATTERN);
        addPatternMatches(activities, message, RIDING_RU_PATTERN);
        addPatternMatches(activities, message, WANT_PATTERN);

        var filtered = new ArrayList<String>();
        for (var activity : activities) {
            var normalized = activity.trim().replaceAll("\\s{2,}", " ");
            if (normalized.length() < 3) {
                continue;
            }
            if (normalized.contains("route") || normalized.contains("маршрут")) {
                continue;
            }
            filtered.add(normalized);
        }
        return filtered;
    }

    private void addPatternMatches(Set<String> target, String message, Pattern pattern) {
        var matcher = pattern.matcher(message);
        while (matcher.find()) {
            var value = matcher.group(1);
            if (value != null && !value.isBlank()) {
                target.add(value.trim());
            }
        }
    }

    private void addIfContains(Set<String> target, String message, String value, String... tokens) {
        if (containsAny(message, tokens)) {
            target.add(value);
        }
    }

    private boolean containsAny(String message, String... tokens) {
        for (var token : tokens) {
            if (message.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
