package ru.fshs.tour.service.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.service.ai.model.RecommendationResult;

@Service
public class RecommendationService {

    public RecommendationResult analyzeRequest(
            List<String> requestedActivities,
            List<Place> places,
            List<Event> events
    ) {
        if (requestedActivities == null || requestedActivities.isEmpty()) {
            return new RecommendationResult(List.of(), List.of());
        }

        var vocabulary = buildVocabulary(places, events);
        var unavailable = new ArrayList<String>();
        var alternatives = new LinkedHashSet<String>();

        for (var requestedActivity : requestedActivities) {
            var tokens = tokenize(requestedActivity);
            if (tokens.isEmpty()) {
                continue;
            }

            if (!isSupported(tokens, vocabulary.tokens)) {
                unavailable.add(requestedActivity);
                suggestAlternatives(requestedActivity, vocabulary.labels).forEach(alternatives::add);
            }
        }

        return new RecommendationResult(List.copyOf(unavailable), List.copyOf(alternatives));
    }

    private Vocabulary buildVocabulary(List<Place> places, List<Event> events) {
        var labels = new LinkedHashSet<String>();
        var tokens = new LinkedHashSet<String>();

        for (var place : places) {
            collectText(place.getName(), labels, tokens);
            collectText(place.getDescription(), labels, tokens);
            collectText(place.getShortDescription(), labels, tokens);
            place.getPlaceTags().forEach(tag -> collectText(tag.getTag().getName(), labels, tokens));
            place.getPlaceCategories().forEach(category -> collectText(category.getCategory().getName(), labels, tokens));
        }

        for (var event : events) {
            collectText(event.getName(), labels, tokens);
            collectText(event.getDescription(), labels, tokens);
            event.getEventTags().forEach(tag -> collectText(tag.getTag().getName(), labels, tokens));
            event.getEventCategories().forEach(category -> collectText(category.getCategory().getName(), labels, tokens));
        }

        return new Vocabulary(List.copyOf(labels), List.copyOf(tokens));
    }

    private void collectText(String value, Set<String> labels, Set<String> tokens) {
        if (value == null || value.isBlank()) {
            return;
        }
        labels.add(value.trim());
        tokens.addAll(tokenize(value));
    }

    private boolean isSupported(List<String> requestedTokens, List<String> knownTokens) {
        if (knownTokens.isEmpty()) {
            return false;
        }
        var knownSet = Set.copyOf(knownTokens);
        for (var token : requestedTokens) {
            if (knownSet.contains(token)) {
                return true;
            }
            for (var knownToken : knownSet) {
                if (knownToken.contains(token) || token.contains(knownToken)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> suggestAlternatives(String requestedActivity, List<String> labels) {
        if (labels.isEmpty()) {
            return List.of();
        }

        var requestedTokens = tokenize(requestedActivity);
        if (requestedTokens.isEmpty()) {
            return List.of();
        }

        var lowerRequested = requestedActivity.toLowerCase(Locale.ROOT);
        var animalRequest = containsAny(lowerRequested, "bear", "animal", "horse", "медвед", "живот");

        return labels.stream()
                .distinct()
                .map(label -> new RankedLabel(label, scoreLabel(label, requestedTokens, animalRequest)))
                .filter(item -> item.score > 0)
                .sorted(Comparator.comparingInt(RankedLabel::score).reversed())
                .limit(3)
                .map(RankedLabel::label)
                .toList();
    }

    private int scoreLabel(String label, List<String> requestedTokens, boolean animalRequest) {
        var lower = label.toLowerCase(Locale.ROOT);
        var labelTokens = tokenize(label);
        int score = 0;

        for (var requested : requestedTokens) {
            for (var labelToken : labelTokens) {
                if (requested.equals(labelToken)) {
                    score += 30;
                } else if (labelToken.contains(requested) || requested.contains(labelToken)) {
                    score += 15;
                } else {
                    score += Math.max(0, 7 - levenshtein(requested, labelToken));
                }
            }
        }

        if (animalRequest && containsAny(lower, "zoo", "safari", "horse", "animal", "конн", "зоо", "сафар")) {
            score += 12;
        }

        return score;
    }

    private List<String> tokenize(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+")).stream()
                .map(String::trim)
                .filter(token -> token.length() >= 3)
                .filter(token -> !isStopToken(token))
                .distinct()
                .toList();
    }

    private boolean isStopToken(String token) {
        return switch (token) {
            case "ride", "riding", "tour", "route", "build", "маршрут", "маршруты", "катание", "хочу", "want",
                 "looking", "for", "ищу" -> true;
            default -> false;
        };
    }

    private boolean containsAny(String value, String... tokens) {
        for (var token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private int levenshtein(String left, String right) {
        var dp = new int[left.length() + 1][right.length() + 1];
        for (int i = 0; i <= left.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= right.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[left.length()][right.length()];
    }

    private record Vocabulary(
            List<String> labels,
            List<String> tokens
    ) {
    }

    private record RankedLabel(
            String label,
            int score
    ) {
    }
}
