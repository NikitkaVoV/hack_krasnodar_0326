package ru.fshs.tour.assistant.conversation.interpreter;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fshs.tour.assistant.ai.config.AssistantAiProperties;
import ru.fshs.tour.assistant.ai.dto.AiJsonRequest;
import ru.fshs.tour.assistant.ai.gateway.AiJsonGateway;
import ru.fshs.tour.assistant.conversation.interpreter.dto.AssistantInterpretationInput;
import ru.fshs.tour.assistant.conversation.interpreter.dto.AiInterpretationResult;
import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;
import ru.fshs.tour.assistant.conversation.interpreter.dto.RequestEntitiesDto;
import ru.fshs.tour.assistant.conversation.interpreter.dto.RequestReferenceDto;
import ru.fshs.tour.assistant.conversation.interpreter.prompt.InterpretationPromptFactory;
import ru.fshs.tour.assistant.exception.AssistantException;
import ru.fshs.tour.assistant.model.AssistantIntent;
import ru.fshs.tour.assistant.model.AssistantSubIntent;

@Service
@RequiredArgsConstructor
public class UserMessageInterpreterImpl implements UserMessageInterpreter {

    private final AiJsonGateway aiJsonGateway;
    private final AssistantAiProperties assistantAiProperties;
    private final InterpretationPromptFactory interpretationPromptFactory;

    @Override
    public NormalizedAssistantRequest interpret(AssistantInterpretationInput input) {
        if (assistantAiProperties.isClassificationEnabled()) {
            var aiResult = classifyWithAi(input);
            if (aiResult != null && aiResult.getIntent() != null && !aiResult.getIntent().isBlank()) {
                return buildFromAiResult(input, aiResult);
            }
        }
        return buildDeterministicFallback(input);
    }

    private NormalizedAssistantRequest buildFromAiResult(AssistantInterpretationInput input, AiInterpretationResult aiResult) {
        var resolvedIntent = parseIntent(aiResult.getIntent());
        var references = mergeReferences(input, aiResult.getReferences());
        var entities = aiResult.getEntities() != null ? aiResult.getEntities() : RequestEntitiesDto.builder().build();

        return NormalizedAssistantRequest.builder()
                .requestId(UUID.randomUUID())
                .sessionId(input.sessionId())
                .userId(input.userId())
                .intent(resolvedIntent)
                .subIntent(resolveSubIntent(aiResult.getSubIntent()))
                .entities(entities)
                .references(references)
                .missingFields(aiResult.getMissingFields() != null ? aiResult.getMissingFields() : List.of())
                .readyForExecution(aiResult.getReadyForExecution() == null || aiResult.getReadyForExecution())
                .originalMessage(input.message())
                .build();
    }

    private NormalizedAssistantRequest buildDeterministicFallback(AssistantInterpretationInput input) {
        String normalizedMessage = input.message() != null
                ? input.message().toLowerCase(Locale.ROOT)
                : "";

        // TODO: replace deterministic mapping with Spring AI structured output generation.
        AssistantIntent intent = resolveIntent(normalizedMessage);

        RequestReferenceDto references = buildReferences(input);
        RequestEntitiesDto entities = RequestEntitiesDto.builder().build();

        interpretationPromptFactory.buildSystemPrompt(input);

        return NormalizedAssistantRequest.builder()
                .requestId(UUID.randomUUID())
                .sessionId(input.sessionId())
                .userId(input.userId())
                .intent(intent)
                .subIntent(AssistantSubIntent.UNKNOWN.name())
                .entities(entities)
                .references(references)
                .missingFields(List.of())
                .readyForExecution(true)
                .originalMessage(input.message())
                .build();
    }

    private AiInterpretationResult classifyWithAi(AssistantInterpretationInput input) {
        var request = AiJsonRequest.builder()
                .systemPrompt(interpretationPromptFactory.buildSystemPrompt(input))
                .userPrompt(interpretationPromptFactory.buildUserPrompt(input))
                .jsonSchemaName("assistant_interpretation")
                .build();
        try {
            return aiJsonGateway.generateJson(request, AiInterpretationResult.class);
        } catch (AssistantException ignored) {
            return null;
        }
    }

    private AssistantIntent resolveIntent(String message) {
        if (message.contains("\u043c\u0430\u0440\u0448\u0440\u0443\u0442") || message.contains("route")) {
            return AssistantIntent.ROUTE_BUILD;
        }
        if (message.contains("\u0440\u044f\u0434\u043e\u043c")
                || message.contains("\u043f\u043e\u0431\u043b\u0438\u0437\u043e\u0441\u0442\u0438")
                || message.contains("nearby")) {
            return AssistantIntent.NEARBY_SUGGEST;
        }
        if (message.contains("\u043f\u0440\u043e\u0444\u0438\u043b")
                || message.contains("\u0431\u044e\u0434\u0436\u0435\u0442")
                || message.contains("\u0432\u043e\u0437\u0440\u0430\u0441\u0442")
                || message.contains("profile")) {
            return AssistantIntent.PROFILE_UPDATE;
        }
        if (message.contains("\u043c\u0435\u0441\u0442\u043e") || message.contains("place")) {
            if (message.contains("\u043e\u043f\u0438\u0448")
                    || message.contains("\u0440\u0430\u0441\u0441\u043a\u0430\u0436")
                    || message.contains("explain")) {
                return AssistantIntent.PLACE_EXPLAIN;
            }
            return AssistantIntent.PLACE_SEARCH;
        }
        if (message.contains("\u0441\u043e\u0431\u044b\u0442") || message.contains("event")) {
            if (message.contains("\u043e\u043f\u0438\u0448")
                    || message.contains("\u0440\u0430\u0441\u0441\u043a\u0430\u0436")
                    || message.contains("explain")) {
                return AssistantIntent.EVENT_EXPLAIN;
            }
            return AssistantIntent.EVENT_SEARCH;
        }
        return AssistantIntent.GENERAL_QUESTION;
    }

    private AssistantIntent parseIntent(String rawIntent) {
        try {
            return AssistantIntent.valueOf(rawIntent.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return AssistantIntent.GENERAL_QUESTION;
        }
    }

    private String resolveSubIntent(String rawSubIntent) {
        if (rawSubIntent == null || rawSubIntent.isBlank()) {
            return AssistantSubIntent.UNKNOWN.name();
        }
        return rawSubIntent;
    }

    private RequestReferenceDto mergeReferences(AssistantInterpretationInput input, RequestReferenceDto aiReferences) {
        var clientContext = input.clientContext();
        return RequestReferenceDto.builder()
                .routeId(firstNonNull(
                        clientContext != null ? clientContext.routeId() : null,
                        aiReferences != null ? aiReferences.getRouteId() : null,
                        input.activeRouteId()
                ))
                .placeId(firstNonNull(
                        clientContext != null ? clientContext.placeId() : null,
                        aiReferences != null ? aiReferences.getPlaceId() : null,
                        input.currentPlaceId()
                ))
                .eventId(firstNonNull(
                        clientContext != null ? clientContext.eventId() : null,
                        aiReferences != null ? aiReferences.getEventId() : null,
                        input.currentEventId()
                ))
                .build();
    }

    private RequestReferenceDto buildReferences(AssistantInterpretationInput input) {
        var clientContext = input.clientContext();
        return RequestReferenceDto.builder()
                .routeId(clientContext != null ? clientContext.routeId() : input.activeRouteId())
                .placeId(clientContext != null ? clientContext.placeId() : input.currentPlaceId())
                .eventId(clientContext != null ? clientContext.eventId() : input.currentEventId())
                .build();
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
