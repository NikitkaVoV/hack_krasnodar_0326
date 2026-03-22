package ru.fshs.tour.assistant.orchestration.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fshs.tour.assistant.api.dto.ClientContextDto;
import ru.fshs.tour.repository.UserConstraintRepository;
import ru.fshs.tour.repository.UserRepository;
import ru.fshs.tour.repository.UserTagRepository;

@Service
@RequiredArgsConstructor
public class AssistantContextServiceImpl implements AssistantContextService {

    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final UserConstraintRepository userConstraintRepository;

    @Override
    public AssistantContext buildContext(UUID userId, ClientContextDto clientContext) {
        var user = userRepository.findById(userId).orElse(null);
        var tags = userTagRepository.findAllByUserId(userId).stream()
                .map(userTag -> userTag.getTag().getName())
                .toList();
        var constraints = userConstraintRepository.findAllByUserId(userId).stream()
                .map(userConstraint -> userConstraint.getConstraint().getName())
                .toList();
        Map<String, String> preferences = new HashMap<>();

        var profileBuilder = UserProfileSnapshot.builder()
                .userId(userId)
                .tags(tags)
                .constraints(constraints)
                .preferences(preferences);

        if (user != null) {
            profileBuilder.age(user.getAge());
            profileBuilder.budgetMin(user.getBudgetMin());
            profileBuilder.budgetMax(user.getBudgetMax());
            profileBuilder.lastLat(user.getLastLocationLat() != null ? user.getLastLocationLat().doubleValue() : null);
            profileBuilder.lastLng(user.getLastLocationLng() != null ? user.getLastLocationLng().doubleValue() : null);
        }

        var profile = profileBuilder.build();

        return AssistantContext.builder()
                .userId(userId)
                .userProfile(profile)
                .activeRouteId(clientContext != null ? clientContext.routeId() : null)
                .currentPlaceId(clientContext != null ? clientContext.placeId() : null)
                .currentEventId(clientContext != null ? clientContext.eventId() : null)
                .build();
    }
}
