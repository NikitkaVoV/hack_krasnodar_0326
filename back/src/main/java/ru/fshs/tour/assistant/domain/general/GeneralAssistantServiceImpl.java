package ru.fshs.tour.assistant.domain.general;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.assistant.conversation.interpreter.dto.RequestEntitiesDto;
import ru.fshs.tour.assistant.domain.common.AssistantResponseType;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.common.NextAction;
import ru.fshs.tour.assistant.model.AssistantStatus;
import ru.fshs.tour.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class GeneralAssistantServiceImpl implements GeneralAssistantService {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AssistantSystemResponse answerQuestion(String message) {
        String normalized = message != null ? message.trim().toLowerCase(Locale.ROOT) : "";

        String summary;
        if (normalized.isBlank()) {
            summary = "Уточните ваш вопрос, чтобы я смог помочь точнее.";
        } else if (normalized.startsWith("\u043f\u0440\u0438\u0432\u0435\u0442")
                || normalized.startsWith("hello")
                || normalized.startsWith("hi")) {
            summary = "Привет! Могу помочь с поиском мест, событий и подбором рядом с вами.";
        } else {
            summary = "Принял общий вопрос. Могу уточнить детали и подобрать подходящие варианты.";
        }

        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(AssistantResponseType.GENERAL_REPLY)
                .summary(summary)
                .payload(message)
                .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                .build();
    }

    @Override
    public AssistantSystemResponse updateProfile(UUID userId, String message, RequestEntitiesDto entities) {
        if (userId == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.NEEDS_CLARIFICATION)
                    .responseType(AssistantResponseType.PROFILE_ACK)
                    .summary("Не удалось определить пользователя для обновления профиля.")
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .build();
        }

        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.FAILED)
                    .responseType(AssistantResponseType.PROFILE_ACK)
                    .summary("Пользователь не найден.")
                    .nextActions(List.of(NextAction.RETRY))
                    .build();
        }

        List<String> updatedFields = new ArrayList<>();
        applyBudgetFromMessage(user, message, updatedFields);
        applyAgeFromMessage(user, message, updatedFields);
        applyCoordinates(user, entities, updatedFields);

        if (updatedFields.isEmpty()) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.PARTIAL)
                    .responseType(AssistantResponseType.PROFILE_ACK)
                    .summary("Не удалось извлечь обновления профиля из сообщения.")
                    .warnings(List.of("Укажите, какие параметры нужно обновить: бюджет, возраст, локация."))
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .build();
        }

        userRepository.save(user);

        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(AssistantResponseType.PROFILE_ACK)
                .summary("Профиль обновлен: " + String.join(", ", updatedFields))
                .payload(updatedFields)
                .nextActions(List.of(NextAction.RETRY))
                .build();
    }

    private void applyBudgetFromMessage(ru.fshs.tour.domain.user.User user, String message, List<String> updatedFields) {
        if (message == null || message.isBlank()) {
            return;
        }
        var matcher = NUMBER_PATTERN.matcher(message);
        List<Integer> numbers = new ArrayList<>();
        while (matcher.find()) {
            try {
                numbers.add(Integer.parseInt(matcher.group()));
            } catch (NumberFormatException ignored) {
                return;
            }
        }

        if (numbers.size() >= 2) {
            int min = Math.min(numbers.get(0), numbers.get(1));
            int max = Math.max(numbers.get(0), numbers.get(1));
            user.setBudgetMin(BigDecimal.valueOf(min));
            user.setBudgetMax(BigDecimal.valueOf(max));
            updatedFields.add("budgetMin");
            updatedFields.add("budgetMax");
        }
    }

    private void applyAgeFromMessage(ru.fshs.tour.domain.user.User user, String message, List<String> updatedFields) {
        if (message == null || message.isBlank()) {
            return;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        if (!normalized.contains("\u0432\u043e\u0437\u0440\u0430\u0441\u0442") && !normalized.contains("age")) {
            return;
        }

        var matcher = NUMBER_PATTERN.matcher(normalized);
        if (matcher.find()) {
            int age = Integer.parseInt(matcher.group());
            if (age > 0 && age < 120) {
                user.setAge(age);
                updatedFields.add("age");
            }
        }
    }

    private void applyCoordinates(ru.fshs.tour.domain.user.User user, RequestEntitiesDto entities, List<String> updatedFields) {
        if (entities == null) {
            return;
        }
        if (entities.getLat() != null) {
            user.setLastLocationLat(BigDecimal.valueOf(entities.getLat()));
            updatedFields.add("lastLocationLat");
        }
        if (entities.getLng() != null) {
            user.setLastLocationLng(BigDecimal.valueOf(entities.getLng()));
            updatedFields.add("lastLocationLng");
        }
    }
}